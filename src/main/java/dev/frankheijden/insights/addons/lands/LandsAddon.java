package dev.frankheijden.insights.addons.lands;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.addons.InsightsAddon;
import dev.frankheijden.insights.api.addons.Region;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import me.angeschossen.lands.api.events.ChunkDeleteEvent;
import me.angeschossen.lands.api.events.ChunkPostClaimEvent;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.ChunkCoordinate;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class LandsAddon implements InsightsAddon, Listener {

    private final LandsIntegration integration;

    public LandsAddon() {
        this.integration = new LandsIntegration(InsightsPlugin.getInstance());
    }

    public String getKey(Land land, World world) {
        return land.getId() + "-" + world.getName();
    }

    public Optional<Region> adapt(Land land, World world) {
        if (land == null) return Optional.empty();

        Collection<ChunkCoordinate> coordinates = land.getChunks(world);
        if (coordinates == null || coordinates.isEmpty()) return Optional.empty();
        return Optional.of(new LandsRegion(coordinates, getKey(land, world)));
    }

    @Override
    public String getPluginName() {
        return "Lands";
    }

    @Override
    public String getAreaName() {
        return "land";
    }

    @Override
    public String getVersion() {
        return "{version}";
    }

    @Override
    public Optional<Region> getRegion(Location location) {
        return adapt(integration.getLand(
                location.getWorld(),
                location.getBlockX() >> 4,
                location.getBlockZ() >> 4
        ), location.getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkPostClaim(ChunkPostClaimEvent event) {
        clearLandCache(event.getLand(), event.getWorld().getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkDelete(ChunkDeleteEvent event) {
        clearLandCache(event.getLand(), event.getWorld());
    }

    private void clearLandCache(Land land, World world) {
        InsightsPlugin.getInstance().getAddonStorage().remove(getKey(land, world));
    }

    public class LandsRegion implements Region {

        private final Collection<ChunkCoordinate> coordinates;
        private final String key;

        public LandsRegion(Collection<ChunkCoordinate> coordinates, String key) {
            this.coordinates = coordinates;
            this.key = key;
        }

        @Override
        public String getAddon() {
            return getPluginName();
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public List<ChunkPart> toChunkParts() {
            List<ChunkPart> parts = new ArrayList<>(coordinates.size());
            for (ChunkCoordinate coordinate : coordinates) {
                parts.add(new ChunkPart(new ChunkLocation(coordinate.getWorld(), coordinate.getX(), coordinate.getZ())));
            }
            return parts;
        }
    }
}
