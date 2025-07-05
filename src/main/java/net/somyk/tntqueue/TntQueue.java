package net.somyk.tntqueue;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
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
	public static Queue<TntEntity> queue = new ConcurrentLinkedQueue<>();
	private static long totalQueued = 0;
	private static long totalDiscarded = 0;

	@Override
	public void onInitialize() {
		ModConfig.load();
		CommandRegistrationCallback.EVENT.register(ModifyConfigCommand::register);

		ServerEntityEvents.ENTITY_LOAD.register(this::onEntityLoad);
		ServerEntityEvents.ENTITY_UNLOAD.register(this::onEntityUnload);
	}

	private void onEntityLoad(Entity entity, ServerWorld serverWorld) {
		if (entity instanceof TntEntity tntEntity) {
			if (activeSet.size() < getIntegerValue(maxPrimedTntAmount)) {
				activeSet.add(tntEntity.getUuid());
			} else {
				if (queue.size() < getIntegerValue(maxQueueSize)) {
					TntEntity clonedTnt = cloneTntEntity(tntEntity, serverWorld);
					if (clonedTnt != null) {
						queue.add(clonedTnt);
						totalQueued++;
						tntEntity.discard();
						LOGGER.debug("TNT queued. Total queued: {}, Queue size: {}", totalQueued, queue.size());
					} else {
						LOGGER.warn("Failed to clone TNT, discarding original.");
						tntEntity.discard();
						totalDiscarded++;
						LOGGER.debug("TNT Discarded. Total discarded: {}", totalDiscarded);
					}
				} else {
					tntEntity.discard();
					totalDiscarded++;
					LOGGER.warn("TNT queue is full, discarding TNT! Total discarded: {}", totalDiscarded);
				}
			}
		}
	}

	private void onEntityUnload(Entity entity, ServerWorld serverWorld) {
		if (entity instanceof TntEntity tntEntity) {
			activeSet.remove(tntEntity.getUuid());
			if (activeSet.size() < getIntegerValue(maxPrimedTntAmount)) {
				TntEntity nextTNT = queue.poll();
				if (nextTNT != null && !nextTNT.isRemoved()) {
					serverWorld.spawnEntity(nextTNT);
					LOGGER.debug("TNT spawned from queue. Queue size: {}", queue.size());
				}
			}
		}
	}

	private TntEntity cloneTntEntity(TntEntity original, ServerWorld world) {
		try {
			TntEntity cloned = new TntEntity(world, original.getX(), original.getY(), original.getZ(), original.getOwner());
			NbtCompound nbt = new NbtCompound();
			original.writeNbt(nbt);
			cloned.readNbt(nbt);
			cloned.setVelocity(original.getVelocity());
			cloned.setFuse(original.getFuse());
			return cloned;
		} catch (Exception e) {
			LOGGER.error("Error cloning TNT entity: ", e);
			return null;
		}
	}

}