package org.imanity.framework.bukkit.util.chunk;

import org.bukkit.ChunkSnapshot;
import org.bukkit.block.Biome;
import org.bukkit.material.MaterialData;
import org.imanity.framework.util.AccessUtil;
import org.imanity.framework.bukkit.reflection.resolver.minecraft.OBCClassResolver;
import org.imanity.framework.bukkit.reflection.wrapper.FieldWrapper;

public class CraftCachedChunk implements CachedChunk {

    private static final FieldWrapper<short[][]> BLOCK_ID_FIELD;
    private static final FieldWrapper<byte[][]> BLOCK_DATA_FIELD;
    private static final FieldWrapper<int[]> HEIGHT_MAP_FIELD;

    static {

        try {
            OBCClassResolver classResolver = new OBCClassResolver();
            Class<?> craftChunkSnapshot = classResolver.resolveSilent("CraftChunkSnapshot");

            BLOCK_ID_FIELD = new FieldWrapper<>(AccessUtil.setAccessible(craftChunkSnapshot.getField("blockids")));
            BLOCK_DATA_FIELD = new FieldWrapper<>(AccessUtil.setAccessible(craftChunkSnapshot.getField("blockdata")));
            HEIGHT_MAP_FIELD = new FieldWrapper<>(AccessUtil.setAccessible(craftChunkSnapshot.getField("hmap")));
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

    }

    public static CachedChunk from(ChunkSnapshot snapshot) {
        return new CraftCachedChunk(snapshot);
    }

    private final ChunkSnapshot snapshot;
    private final short[][] blockids;
    private final byte[][] blockdata;
    private final int[] hmap;

    private CraftCachedChunk(ChunkSnapshot snapshot) {
        this.snapshot = snapshot;
        this.blockids = BLOCK_ID_FIELD.get(this.snapshot);
        this.blockdata = BLOCK_DATA_FIELD.get(this.snapshot);
        this.hmap = HEIGHT_MAP_FIELD.get(this.snapshot);
    }

    public int getX() {
        return this.snapshot.getX();
    }

    public int getZ() {
        return this.snapshot.getZ();
    }

    public String getWorldName() {
        return this.snapshot.getWorldName();
    }

    @Override
    public MaterialData getBlock(int x, int y, int z) {
        return new MaterialData(this.getBlockTypeId(x, y, z), (byte) this.getBlockData(x, y, z));
    }

    public final void setBlock(int x, int y, int z, MaterialData materialData) {
        this.blockids[y >> 4][(y & 15) << 8 | z << 4 | x] = (short) materialData.getItemTypeId();

        int var4 = (y & 15) << 7 | z << 3 | x >> 1;

        int jj = var4 >> 1;

        int data = materialData.getData();

        if ((var4 & 1) == 0) {
            this.blockdata[y >> 4][jj] = (byte) ((this.blockdata[y >> 4][jj] & 0xF0) | (data & 0xF));
        } else {
            this.blockdata[y >> 4][jj] = (byte) ((this.blockdata[y >> 4][jj] & 0xF) | ((data & 0xF) << 4));
        }

        if (this.getHighestBlockYAt(x, z) < y) {
            this.hmap[z << 4 | x] = y;
        }
    }

    public final int getBlockTypeId(int var1, int var2, int var3) {
        return this.blockids[var2 >> 4][(var2 & 15) << 8 | var3 << 4 | var1];
    }

    public final int getBlockData(int var1, int var2, int var3) {
        int var4 = (var2 & 15) << 7 | var3 << 3 | var1 >> 1;
        return this.blockdata[var2 >> 4][var4] >> ((var1 & 1) << 2) & 15;
    }

    public final int getBlockSkyLight(int var1, int var2, int var3) {
        return this.snapshot.getBlockSkyLight(var1, var2, var3)
;    }

    public final int getBlockEmittedLight(int var1, int var2, int var3) {
        return this.snapshot.getBlockEmittedLight(var1, var2, var3);
    }

    public final int getHighestBlockYAt(int var1, int var2) {
        return this.hmap[var2 << 4 | var1];
    }

    public final Biome getBiome(int var1, int var2) {
        return this.snapshot.getBiome(var1, var2);
    }

    public final double getRawBiomeTemperature(int var1, int var2) {
        return this.snapshot.getRawBiomeTemperature(var1, var2);
    }

    public final double getRawBiomeRainfall(int var1, int var2) {
        return this.snapshot.getRawBiomeRainfall(var1, var2);
    }

    public final long getCaptureFullTime() {
        return this.snapshot.getCaptureFullTime();
    }

    public final boolean isSectionEmpty(int var1) {
        return this.snapshot.isSectionEmpty(var1);
    }

}
