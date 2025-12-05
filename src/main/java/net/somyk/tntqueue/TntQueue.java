package net.somyk.tntqueue;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.somyk.tntqueue.command.ModifyConfigCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static net.somyk.tntqueue.ModConfig.*;

public class TntQueue implements ModInitializer {
	public static final String MOD_ID = "TntQueue";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Queue<UUID> activeSet = new ConcurrentLinkedQueue<>();
	public static Queue<PrimedTnt> queue = new ConcurrentLinkedQueue<>();
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
		if (entity instanceof PrimedTnt PrimedTnt) {
			if (activeSet.size() < getIntegerValue(maxPrimedTntAmount)) {
				activeSet.add(PrimedTnt.getUUID());
			} else {
				if (queue.size() < getIntegerValue(maxQueueSize)) {
          PrimedTnt clonedTnt = clonePrimedTnt(PrimedTnt, serverLevel);
					if (clonedTnt != null) {
						queue.add(clonedTnt);
						totalQueued++;
						PrimedTnt.discard();
						LOGGER.debug("TNT queued. Total queued: {}, Queue size: {}", totalQueued, queue.size());
					} else {
						LOGGER.warn("Failed to clone TNT, discarding original.");
						PrimedTnt.discard();
						totalDiscarded++;
						LOGGER.debug("TNT Discarded. Total discarded: {}", totalDiscarded);
					}
				} else {
					PrimedTnt.discard();
					totalDiscarded++;
					LOGGER.warn("TNT queue is full, discarding TNT! Total discarded: {}", totalDiscarded);
				}
			}
		}
	}

	private void onEntityUnload(Entity entity, ServerLevel serverWorld) {
		if (entity instanceof PrimedTnt PrimedTnt) {
			activeSet.remove(PrimedTnt.getUUID());
			if (activeSet.size() < getIntegerValue(maxPrimedTntAmount)) {
				PrimedTnt nextTNT = queue.poll();
				if (nextTNT != null && !nextTNT.isRemoved()) {
					serverWorld.addFreshEntity(nextTNT);
					LOGGER.debug("TNT spawned from queue. Queue size: {}", queue.size());
				}
			}
		}
	}

	private PrimedTnt clonePrimedTnt(PrimedTnt original, ServerLevel world) {
		try {
      PrimedTnt cloned = new PrimedTnt(world, original.getX(), original.getY(), original.getZ(), original.getOwner());
			TagValueOutput writeView = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, world.registryAccess());
			original.saveWithoutId(writeView);
      ValueInput readView =
          TagValueInput.create(
              ProblemReporter.DISCARDING,
              world.registryAccess(),
              writeView.buildResult().copy());
			cloned.load(readView);
			cloned.setDeltaMovement(original.getDeltaMovement());
			cloned.setFuse(original.getFuse());
			return cloned;
		} catch (Exception e) {
			LOGGER.error("Error cloning TNT entity: ", e);
			return null;
		}
	}

}