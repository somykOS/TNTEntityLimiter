package net.somyk.tntqueue;

import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;
import net.somyk.tntqueue.command.ModifyConfigCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.somyk.tntqueue.ModConfig.*;

public class TntQueue implements ModInitializer {
  public static final String MOD_ID = "TntQueue";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
  public static final boolean IS_DEV = FabricLoader.getInstance().isDevelopmentEnvironment();

  private static final Set<UUID> trackedTntEntities = ConcurrentHashMap.newKeySet();
  public static Queue<TntData> queue = new ConcurrentLinkedQueue<>();
  private static long totalQueued = 0;
  private static long totalDiscarded = 0;

  @Override
  public void onInitialize() {
    ModConfig.load();
    CommandRegistrationCallback.EVENT.register(ModifyConfigCommand::register);

    ServerEntityEvents.ENTITY_LOAD.register(this::onEntityLoad);
    ServerEntityEvents.ENTITY_UNLOAD.register(this::onEntityUnload);
  }

  private void onEntityLoad(Entity entity, ServerLevel serverLevel) {
    if (entity instanceof PrimedTnt tnt) {
      if (trackedTntEntities.size() < getIntegerValue(maxPrimedTntAmount)) {
        trackedTntEntities.add(tnt.getUUID());
      } else {
        if (queue.size() < getIntegerValue(maxQueueSize)) {
          TntData data =
              new TntData(
                  tnt.getX(),
                  tnt.getY(),
                  tnt.getZ(),
                  tnt.getOwner(),
                  tnt.getDeltaMovement(),
                  tnt.getFuse(),
                  System.currentTimeMillis());
          queue.add(data);
          totalQueued++;
          serverLevel.getServer().execute(() -> {
            if (tnt.isAlive()) {
              tnt.remove(Entity.RemovalReason.DISCARDED);
            }
          });
          if(IS_DEV) LOGGER.info("TNT queued. Total queued: {}, Queue size: {}", totalQueued, queue.size());
        } else {
          serverLevel.getServer().execute(() -> {
            if (tnt.isAlive()) {
              tnt.remove(Entity.RemovalReason.DISCARDED);
            }
          });
          totalDiscarded++;
          if(IS_DEV) LOGGER.warn("TNT queue is full, discarding TNT! Total discarded: {}", totalDiscarded);
        }
      }
    }
  }

  private void onEntityUnload(Entity entity, ServerLevel serverWorld) {
    if (entity instanceof PrimedTnt tnt) {
      if (trackedTntEntities.remove(tnt.getUUID())) {
        if (trackedTntEntities.size() < getIntegerValue(maxPrimedTntAmount)) {
          TntData data = queue.poll();
          if (data != null) {
            PrimedTnt newTnt = new PrimedTnt(serverWorld, data.x(), data.y(), data.z(), data.owner());
            newTnt.setDeltaMovement(data.delta());
            newTnt.setFuse(20);
            serverWorld.addFreshEntity(newTnt);
            trackedTntEntities.add(newTnt.getUUID());
            if(IS_DEV) LOGGER.debug("TNT spawned from queue. Queue size: {}", queue.size());
          }
        }
      }
    }
  }

  //	private PrimedTnt clonePrimedTnt(PrimedTnt original, ServerLevel world) {
  //		try {
  //      PrimedTnt cloned = new PrimedTnt(world, original.getX(), original.getY(), original.getZ(),
  // original.getOwner());
  //			TagValueOutput writeView = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,
  // world.registryAccess());
  //			original.saveWithoutId(writeView);
  //      ValueInput readView =
  //          TagValueInput.create(
  //              ProblemReporter.DISCARDING,
  //              world.registryAccess(),
  //              writeView.buildResult().copy());
  //			cloned.load(readView);
  //			cloned.setDeltaMovement(original.getDeltaMovement());
  //			cloned.setFuse(original.getFuse());
  //			return cloned;
  //		} catch (Exception e) {
  //			LOGGER.error("Error cloning TNT entity: ", e);
  //			return null;
  //		}
  //	}

  public record TntData(
      double x, double y, double z, LivingEntity owner, Vec3 delta, int fuse, long addTime) {}
}