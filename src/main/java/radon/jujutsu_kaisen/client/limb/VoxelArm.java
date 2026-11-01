package radon.jujutsu_kaisen.client.limb;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import radon.jujutsu_kaisen.JujutsuKaisen;

import java.util.ArrayDeque;

public final class VoxelArm {
    public static final ResourceLocation INTERIOR =
            new ResourceLocation(JujutsuKaisen.MOD_ID, "textures/entity/limb_interior.png");

    private static final float PIXEL = 1.0F / 16.0F;

    private static final float GROW_MIN = 0.05F;

    private static final float BONE_REACH = 0.85F;
    private static final float MEAT_REACH = 1.15F;

    private static final float BONE_RATIO = 2.0F;

    private static final float TORN_LEAD = 3.5F;

    private static final float FRAY = 2.0F;

    private VoxelArm() {
    }

    public static void render(PoseStack pose, MultiBufferSource buffers, int light, ResourceLocation skin,
                              ModelPart arm, ModelPart sleeve, float remaining, float cut, boolean torn, long seed) {
        if (remaining >= 1.0F || remaining <= 0.0F) return;

        CubeFaces base = CubeFaces.of(arm);
        CubeFaces outer = CubeFaces.of(sleeve);

        int width = Math.round(base.maxX - base.minX);
        int height = Math.round(base.maxY - base.minY);
        int depth = Math.round(base.maxZ - base.minZ);

        if (width <= 0 || height <= 0 || depth <= 0) return;

        Grid grid = Grid.of(width, height, depth, remaining, cut, torn, seed);

        VertexConsumer skinBuffer = buffers.getBuffer(RenderType.entityTranslucent(skin));

        emit(pose, skinBuffer, light, base, grid, Pass.SURFACE);

        emit(pose, skinBuffer, light, outer, grid, Pass.SURFACE);

        VertexConsumer interior = buffers.getBuffer(RenderType.entityCutoutNoCull(INTERIOR));

        emit(pose, interior, light, base, grid, Pass.INTERIOR);
    }

    private enum Pass {
        SURFACE,
        INTERIOR
    }

    private static final class Grid {
        final int width, height, depth;

        final float[][][] fill;

        private Grid(int width, int height, int depth, float[][][] fill) {
            this.width = width;
            this.height = height;
            this.depth = depth;
            this.fill = fill;
        }

        static Grid of(int width, int height, int depth, float remaining, float cut, boolean torn, long seed) {
            float room = 1.0F - cut;
            float grown = room <= 0.0F ? 1.0F : Mth.clamp((remaining - cut) / room, 0.0F, 1.0F);

            float bone = (cut + room * (float) Math.pow(grown, BONE_REACH)) * height;
            float meat = (cut + room * (float) Math.pow(grown, MEAT_REACH)) * height;

            bone += torn ? TORN_LEAD * (1.0F - grown) : 0.0F;

            bone = Math.min(bone, meat * BONE_RATIO);

            float fray = torn
                    ? Math.min(FRAY * (1.0F - grown), Math.max(0.0F, meat - 1.0F))
                    : 0.0F;

            float[][][] fill = new float[width][height][depth];

            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    boolean core = core(width, depth, x, z);
                    float front = core ? bone : meat;

                    for (int y = 0; y < height; y++) {
                        float amount;

                        if (core || fray <= 0.0F) {
                            amount = Mth.clamp(front - y, 0.0F, 1.0F);
                        } else {
                            amount = noise(seed, x, y, z) < Mth.clamp((front - y) / fray, 0.0F, 1.0F)
                                    ? 1.0F : 0.0F;
                        }

                        fill[x][y][z] = amount < GROW_MIN ? 0.0F : amount;
                    }
                }
            }

            prune(fill, width, height, depth);

            return new Grid(width, height, depth, fill);
        }

        boolean has(int x, int y, int z) {
            return this.inside(x, y, z) && this.fill[x][y][z] > 0.0F;
        }

        float tall(int x, int y, int z) {
            return this.fill[x][y][z];
        }

        boolean inside(int x, int y, int z) {
            return x >= 0 && x < this.width && y >= 0 && y < this.height && z >= 0 && z < this.depth;
        }
    }

    private static void prune(float[][][] fill, int width, int height, int depth) {
        boolean[][][] joined = new boolean[width][height][depth];
        ArrayDeque<int[]> pending = new ArrayDeque<>();

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                if (fill[x][0][z] <= 0.0F) continue;

                joined[x][0][z] = true;
                pending.add(new int[]{x, 0, z});
            }
        }

        while (!pending.isEmpty()) {
            int[] at = pending.removeFirst();

            for (Direction d : Direction.values()) {
                int nx = at[0] + d.getStepX(), ny = at[1] + d.getStepY(), nz = at[2] + d.getStepZ();

                if (nx < 0 || nx >= width || ny < 0 || ny >= height || nz < 0 || nz >= depth) continue;
                if (joined[nx][ny][nz] || fill[nx][ny][nz] <= 0.0F) continue;

                joined[nx][ny][nz] = true;
                pending.add(new int[]{nx, ny, nz});
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (!joined[x][y][z]) fill[x][y][z] = 0.0F;
                }
            }
        }
    }

    private static float noise(long seed, int x, int y, int z) {
        long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (y * 0xBF58476D1CE4E5B9L) ^ (z * 0xC2B2AE3D27D4EB4FL);
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;

        return (h >>> 40) / (float) (1 << 24);
    }

    private static boolean core(int width, int depth, int x, int z) {
        boolean coreX = width >= 4 ? (x == 1 || x == 2) : x == width / 2;
        boolean coreZ = depth >= 4 ? (z == 1 || z == 2) : z == depth / 2;

        return coreX && coreZ;
    }

    private static void emit(PoseStack pose, VertexConsumer buffer, int light, CubeFaces box, Grid grid, Pass pass) {
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();

        float sx = (box.maxX - box.minX) / grid.width;
        float sy = (box.maxY - box.minY) / grid.height;
        float sz = (box.maxZ - box.minZ) / grid.depth;

        for (int x = 0; x < grid.width; x++) {
            for (int y = 0; y < grid.height; y++) {
                for (int z = 0; z < grid.depth; z++) {
                    if (!grid.has(x, y, z)) continue;

                    float tall = grid.tall(x, y, z);

                    float x0 = box.minX + x * sx, x1 = x0 + sx;
                    float y0 = box.minY + y * sy, y1 = y0 + sy * tall;
                    float z0 = box.minZ + z * sz, z1 = z0 + sz;

                    for (Direction d : Direction.values()) {
                        int nx = x + d.getStepX(), ny = y + d.getStepY(), nz = z + d.getStepZ();

                        if (grid.has(nx, ny, nz)) continue;

                        boolean surface = !grid.inside(nx, ny, nz);

                        if (surface != (pass == Pass.SURFACE)) continue;

                        if (surface) {
                            float[] uv = box.uv(d, centre(d, x0, x1, Direction.Axis.X),
                                    centre(d, y0, y1, Direction.Axis.Y), centre(d, z0, z1, Direction.Axis.Z));

                            quad(buffer, matrix, normal, light, d, x0, y0, z0, x1, y1, z1, uv[0], uv[1], 1.0F, 1.0F, 1.0F);
                        } else {
                            float[] c = flesh(grid.width, grid.depth, x, z, y);

                            quad(buffer, matrix, normal, light, d, x0, y0, z0, x1, y1, z1, 0.5F, 0.5F, c[0], c[1], c[2]);
                        }
                    }
                }
            }
        }
    }

    private static float centre(Direction d, float lo, float hi, Direction.Axis axis) {
        if (d.getAxis() != axis) return (lo + hi) / 2.0F;

        return d.getAxisDirection() == Direction.AxisDirection.POSITIVE ? hi : lo;
    }

    private static float[] flesh(int width, int depth, int x, int z, int y) {
        float n = ((x * 7 + z * 13 + y * 3) % 5) / 5.0F;

        return core(width, depth, x, z)
                ? new float[]{0.92F + 0.08F * n, 0.90F + 0.08F * n, 0.84F + 0.08F * n}
                : new float[]{0.55F + 0.20F * n, 0.06F + 0.04F * n, 0.06F + 0.02F * n};
    }

    private static void quad(VertexConsumer buffer, Matrix4f matrix, Matrix3f normal, int light, Direction d,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float u, float v, float r, float g, float b) {
        float nx = d.getStepX(), ny = d.getStepY(), nz = d.getStepZ();

        switch (d) {
            case DOWN -> {
                vertex(buffer, matrix, normal, light, x0, y0, z1, u, v, r, g, b, nx, ny, nz);
                vertex(buffer, matrix, normal, light, x0, y0, z0, u, v, r, g, b, nx, ny, nz);
                vertex(buffer, matrix, normal, light, x1, y0, z0, u, v, r, g, b, nx, ny, nz);
                vertex(buffer, matrix, normal, light, x1, y0, z1, u, v, r, g, b, nx, ny, nz);
            }
            case UP -> {
                vertex(buffer, matrix, normal, light, x0, y1, z0, u, v, r, g, b, nx, ny, nz);
                vertex(buffer, matrix, normal, light, x0, y1, z1, u, v, r, g, b, nx, ny, nz);
                vertex(buffer, matrix, normal, light, x1, y1, z1, u, v, r, g, b, nx, ny, nz);
                vertex(buffer, matrix, normal, light, x1, y1, z0, u, v, r, g, b, nx, ny, nz);
            }
            case NORTH -> {
                vertex(buffer, matrix, normal, light, x1, y0, z0, u, v, r, g, b, nx, ny, nz);
                vertex(buffer, matrix, normal, light, x0, y0, z0, u, v, r, g, b, nx, ny, nz);
                vertex(buffer, matrix, normal, light, x0, y1, z0, u, v, r, g, b, nx, ny, nz);
                vertex(buffer, matrix, normal, light, x1, y1, z0, u, v, r, g, b, nx, ny, nz);
            }
            case SOUTH -> {
                vertex(buffer, matrix, normal, light, x0, y0, z1, u, v, r, g, b, nx, ny, nz);
                vertex(buffer, matrix, normal, light, x1, y0, z1, u, v, r, g, b, nx, ny, nz);
                vertex(buffer, matrix, normal, light, x1, y1, z1, u, v, r, g, b, nx, ny, nz);
                vertex(buffer, matrix, normal, light, x0, y1, z1, u, v, r, g, b, nx, ny, nz);
            }
            case WEST -> {
                vertex(buffer, matrix, normal, light, x0, y0, z0, u, v, r, g, b, nx, ny, nz);
                vertex(buffer, matrix, normal, light, x0, y0, z1, u, v, r, g, b, nx, ny, nz);
                vertex(buffer, matrix, normal, light, x0, y1, z1, u, v, r, g, b, nx, ny, nz);
                vertex(buffer, matrix, normal, light, x0, y1, z0, u, v, r, g, b, nx, ny, nz);
            }
            case EAST -> {
                vertex(buffer, matrix, normal, light, x1, y0, z1, u, v, r, g, b, nx, ny, nz);
                vertex(buffer, matrix, normal, light, x1, y0, z0, u, v, r, g, b, nx, ny, nz);
                vertex(buffer, matrix, normal, light, x1, y1, z0, u, v, r, g, b, nx, ny, nz);
                vertex(buffer, matrix, normal, light, x1, y1, z1, u, v, r, g, b, nx, ny, nz);
            }
        }
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, Matrix3f normal, int light,
                               float x, float y, float z, float u, float v, float r, float g, float b,
                               float nx, float ny, float nz) {
        buffer.vertex(matrix, x * PIXEL, y * PIXEL, z * PIXEL)
                .color(r, g, b, 1.0F)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, nx, ny, nz)
                .endVertex();
    }
}
