package servermod.worldedit;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.BlockSign;
import net.minecraft.src.Chunk;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityAnimal;
import net.minecraft.src.EntityGolem;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityLightningBolt;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityTameable;
import net.minecraft.src.EntityVillager;
import net.minecraft.src.EnumSkyBlock;
import net.minecraft.src.IAnimals;
import net.minecraft.src.IInventory;
import net.minecraft.src.IMob;
import net.minecraft.src.INpc;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityFurnace;
import net.minecraft.src.TileEntityMobSpawner;
import net.minecraft.src.TileEntityNote;
import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;
import net.minecraft.src.WorldGenBigMushroom;
import net.minecraft.src.WorldGenBigTree;
import net.minecraft.src.WorldGenForest;
import net.minecraft.src.WorldGenHugeTrees;
import net.minecraft.src.WorldGenTrees;
import net.minecraft.src.WorldServer;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EntityType;
import com.sk89q.worldedit.UnknownBiomeTypeException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.ContainerBlock;
import com.sk89q.worldedit.blocks.FurnaceBlock;
import com.sk89q.worldedit.blocks.MobSpawnerBlock;
import com.sk89q.worldedit.blocks.NoteBlock;
import com.sk89q.worldedit.blocks.SignBlock;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;

public class LocalWorld extends com.sk89q.worldedit.LocalWorld {
	private final World world;
	
	public LocalWorld(World world) {
		this.world = world;
	}
	
	@Override
	public boolean clearContainerBlockContents(Vector arg0) {
		TileEntity te = world.getBlockTileEntity(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ());
		
		if (te instanceof IInventory) {
			IInventory inv = (IInventory)te;
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				inv.setInventorySlotContents(i, null);
			}
			
			return true;
		} else return false;
	}

	@Override
	public boolean copyFromWorld(Vector arg0, BaseBlock arg1) {
		if (arg1 instanceof SignBlock) {
			TileEntity te = world.getBlockTileEntity(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ());
			if (!(te instanceof TileEntitySign)) return false;
			
			((SignBlock)arg1).setText(((TileEntitySign)te).signText.clone());
			return true;
		}
		
		if (arg1 instanceof FurnaceBlock) {
			TileEntity te = world.getBlockTileEntity(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ());
			if (!(te instanceof TileEntityFurnace)) return false;
			
			FurnaceBlock we = (FurnaceBlock)arg1;
			TileEntityFurnace furnace = (TileEntityFurnace)te;
			we.setBurnTime((short)furnace.furnaceBurnTime);
			we.setCookTime((short)furnace.furnaceCookTime);
			we.setItems(WorldEditUtils.inventoryToBaseItemStack(furnace));
			return true;
		}
		
		if (arg1 instanceof ContainerBlock) {
			TileEntity te = world.getBlockTileEntity(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ());
			if (!(te instanceof IInventory)) return false;
			
			((ContainerBlock)arg1).setItems(WorldEditUtils.inventoryToBaseItemStack((IInventory)te));
			return true;
		}
		
		if (arg1 instanceof MobSpawnerBlock) {
			TileEntity te = world.getBlockTileEntity(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ());
			if (!(te instanceof TileEntityMobSpawner)) return false;
			
			MobSpawnerBlock we = (MobSpawnerBlock)arg1;
			TileEntityMobSpawner spawner = (TileEntityMobSpawner)te;
			we.setMobType(spawner.getMobID());
			we.setDelay((short)spawner.delay);
			return true;
		}
		
		if (arg1 instanceof NoteBlock) {
			TileEntity te = world.getBlockTileEntity(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ());
			if (!(te instanceof TileEntityNote)) return false;
			
			((NoteBlock)arg1).setNote(((TileEntityNote)te).note);
			return true;
		}
		
		return false;
	}

	@Override
	public boolean copyToWorld(Vector arg0, BaseBlock arg1) {
		if (arg1 instanceof SignBlock) {
			TileEntity te = world.getBlockTileEntity(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ());
			if (!(te instanceof TileEntitySign)) return false;
			
			((TileEntitySign)te).signText = ((SignBlock)arg1).getText().clone();
			return true;
		}
		
		if (arg1 instanceof FurnaceBlock) {
			TileEntity te = world.getBlockTileEntity(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ());
			if (!(te instanceof TileEntityFurnace)) return false;
			
			FurnaceBlock we = (FurnaceBlock)arg1;
			TileEntityFurnace furnace = (TileEntityFurnace)te;
			furnace.furnaceBurnTime = we.getBurnTime();
			furnace.furnaceCookTime = we.getCookTime();
			WorldEditUtils.baseItemStackToInventory(we.getItems(), furnace);
			return true;
		}
		
		if (arg1 instanceof ContainerBlock) {
			TileEntity te = world.getBlockTileEntity(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ());
			if (!(te instanceof IInventory)) return false;
			
			WorldEditUtils.baseItemStackToInventory(((ContainerBlock)arg1).getItems(), (IInventory)te);
			return true;
		}
		
		if (arg1 instanceof MobSpawnerBlock) {
			TileEntity te = world.getBlockTileEntity(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ());
			if (!(te instanceof TileEntityMobSpawner)) return false;
			
			MobSpawnerBlock we = (MobSpawnerBlock)arg1;
			TileEntityMobSpawner spawner = (TileEntityMobSpawner)te;
			spawner.setMobID(we.getMobType());
			spawner.delay = we.getDelay();
			return true;
		}
		
		if (arg1 instanceof NoteBlock) {
			TileEntity te = world.getBlockTileEntity(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ());
			if (!(te instanceof TileEntityNote)) return false;
			
			((TileEntityNote)te).note = ((NoteBlock)arg1).getNote();
			return true;
		}
		
		return false;
	}

	@Override
	public void dropItem(Vector arg0, BaseItemStack arg1) {
		world.spawnEntityInWorld(new EntityItem(world, arg0.getX(), arg0.getY(), arg0.getZ(), WorldEditUtils.baseItemStackToStack(arg1)));
	}

	@Override
	public boolean equals(Object arg0) {
		return arg0 instanceof LocalWorld && ((LocalWorld)arg0).world.equals(world);
	}

	@Override
	public BiomeType getBiome(Vector2D arg0) {
		try {
			return WorldEdit.instance.serverInterface.getBiomes().get(world.getBiomeGenForCoords(arg0.getBlockX(), arg0.getBlockZ()).biomeName);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public int getBlockData(Vector arg0) {
		return world.getBlockMetadata(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ());
	}

	@Override
	public int getBlockLightLevel(Vector arg0) {
		return world.getChunkFromBlockCoords(arg0.getBlockX(), arg0.getBlockZ()).getSavedLightValue(EnumSkyBlock.Block, arg0.getBlockX() & 16, arg0.getBlockY(), arg0.getBlockZ() & 16);
	}

	@Override
	public int getBlockType(Vector arg0) {
		return world.getBlockId(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ());
	}

	@Override
	public String getName() {
		return world.provider.getDimensionName();
	}

	@Override
	public int hashCode() {
		return world.hashCode();
	}

	@Override
	public boolean regenerate(Region arg0, EditSession arg1) {
        return false;
	}

	@Override
	public int removeEntities(EntityType arg0, Vector arg1, int arg2) {
		int count = 0;
		
		for (Entity ent : (List<Entity>)world.getEntitiesWithinAABB(WorldEditUtils.getEntityType(arg0), AxisAlignedBB.getBoundingBox(arg1.getX() - arg2, arg1.getY() - arg2, arg1.getZ() - arg2, arg1.getX() + arg2, arg1.getY() + arg2, arg1.getZ() + arg2))) {
			ent.setDead();
			count++;
		}
		
		return count;
	}
	
	@Override
	public int killMobs(Vector arg1, double arg2, int flags) {
		boolean killPets = (flags & KillFlags.PETS) != 0;
        boolean killNPCs = (flags & KillFlags.NPCS) != 0;
        boolean killAnimals = (flags & KillFlags.ANIMALS) != 0;
        boolean withLightning = (flags & KillFlags.WITH_LIGHTNING) != 0;
        boolean killGolems = (flags & KillFlags.GOLEMS) != 0;
        
        int count = 0;
        
        for (Entity ent : (List<Entity>)world.getEntitiesWithinAABB(EntityLiving.class, AxisAlignedBB.getBoundingBox(arg1.getX() - arg2, arg1.getY() - arg2, arg1.getZ() - arg2, arg1.getX() + arg2, arg1.getY() + arg2, arg1.getZ() + arg2))) {
        	if (ent instanceof IMob ||
        		(killPets && ent instanceof EntityTameable) ||
        		(killNPCs && ent instanceof INpc) ||
        		(killAnimals && ent instanceof IAnimals) ||
        		(killGolems && ent instanceof EntityGolem)
        		) {
        		if (withLightning) world.addWeatherEffect(new EntityLightningBolt(world, ent.posX, ent.posY, ent.posZ));
        		ent.setDead();
        		count++;
        	}
		}
        
        return count;
	}

	@Override
	public void setBiome(Vector2D arg0, BiomeType arg1) {
		Chunk chunk = world.getChunkFromBlockCoords(arg0.getBlockX(), arg0.getBlockZ());
		byte[] array = chunk.getBiomeArray();
		array[(arg0.getBlockX() & 16) << 4 | (arg0.getBlockZ() & 16)] = (byte)(((servermod.worldedit.BiomeType)arg1).biome.biomeID & 255);
		chunk.setBiomeArray(array);
	}

    @Override
    public boolean setBlock(Vector arg0, com.sk89q.worldedit.foundation.Block arg1, boolean arg2) {
        boolean idset;
        if (arg2) {
        	idset = world.setBlockAndMetadataWithNotify(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ(), arg1.getId(), arg1.getData());
        } else {
        	idset = world.setBlockAndMetadata(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ(), arg1.getId(), arg1.getData());
        }
        
        if (arg1 instanceof BaseBlock) {
            copyToWorld(arg0, (BaseBlock)arg1);
        }
        
        return !idset;
    }

	@Override
	public void setBlockData(Vector arg0, int arg1) {
		world.setBlockMetadataWithNotify(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ(), arg1);
	}

	@Override
	public void setBlockDataFast(Vector arg0, int arg1) {
		world.setBlockMetadata(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ(), arg1);
	}

	@Override
	public boolean setBlockType(Vector arg0, int arg1) {
		return !world.setBlock(arg0.getBlockX(), arg0.getBlockY(), arg0.getBlockZ(), arg1);
	}
	
	@Override
	public boolean generateTree(TreeType arg0, EditSession arg1, Vector arg2) {
		switch (arg0) {
			case BIRCH: return new WorldGenForest(true).generate(world, world.rand, arg2.getBlockX(), arg2.getBlockY(), arg2.getBlockZ());
			case REDWOOD: return new WorldGenTrees(true, 4 + world.rand.nextInt(7), 3, 3, false).generate(world, world.rand, arg2.getBlockX(), arg2.getBlockY(), arg2.getBlockZ());
			case BIG_TREE: return new WorldGenHugeTrees(true, 10 + world.rand.nextInt(20), 3, 3).generate(world, world.rand, arg2.getBlockX(), arg2.getBlockY(), arg2.getBlockZ());
			case BROWN_MUSHROOM: return new WorldGenBigMushroom(0).generate(world, world.rand, arg2.getBlockX(), arg2.getBlockY(), arg2.getBlockZ());
			case RED_MUSHROOM: return new WorldGenBigMushroom(1).generate(world, world.rand, arg2.getBlockX(), arg2.getBlockY(), arg2.getBlockZ());
			default: return (world.rand.nextInt(10) == 0 ? new WorldGenBigTree(true) : new WorldGenTrees(true)).generate(world, world.rand, arg2.getBlockX(), arg2.getBlockY(), arg2.getBlockZ());
		}
	}
	
	@Override
	public void checkLoadedChunk(Vector arg0) {
		world.getChunkProvider().provideChunk(arg0.getBlockX() >> 4, arg0.getBlockZ() >> 4);
    }
	
	@Override
	public LocalEntity[] getEntities(Region arg0) {
		ArrayList<LocalEntity> ret = new ArrayList<LocalEntity>();
		
		Vector min = arg0.getMinimumPoint();
		Vector max = arg0.getMaximumPoint();
		for (Entity entity : (List<Entity>)world.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ()))) {
			ret.add(WorldEdit.instance.getEntity(entity));
		}
		
		return ret.toArray(new LocalEntity[0]);
	}
	
	@Override
	public int killEntities(com.sk89q.worldedit.LocalEntity... entities) {
		for (com.sk89q.worldedit.LocalEntity entity : entities) ((LocalEntity)entity).entity.setDead();
		return entities.length;
	}
	
	@Override
	public void fixAfterFastMode(Iterable<BlockVector2D> chunks) {
		for (BlockVector2D chunk : chunks) ((WorldServer)world).getPlayerManager().flagChunkForUpdate(chunk.getBlockX(), 0, chunk.getBlockZ());
    }
}
