package radon.jujutsu_kaisen.client.limb;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class CubeFaces {
    private static final Map<ModelPart, CubeFaces> CACHE = new WeakHashMap<>();

    public final float minX, minY, minZ, maxX, maxY, maxZ;

    private final Face[] faces = new Face[6];

    private static final class Face {
        final Vector3f a;
        final Vector3f ab;
        final Vector3f ac;
        final float au, av, bu, bv, cu, cv;
        final float ab2, ac2;

        Face(Corner a, Corner b, Corner c) {
            this.a = a.pos;
            this.ab = new Vector3f(b.pos).sub(a.pos);
            this.ac = new Vector3f(c.pos).sub(a.pos);
            this.au = a.u;
            this.av = a.v;
            this.bu = b.u;
            this.bv = b.v;
            this.cu = c.u;
            this.cv = c.v;
            this.ab2 = this.ab.lengthSquared();
            this.ac2 = this.ac.lengthSquared();
        }

        float[] uv(float x, float y, float z) {
            float px = x - this.a.x, py = y - this.a.y, pz = z - this.a.z;

            float tu = this.ab2 == 0.0F ? 0.0F : (px * this.ab.x + py * this.ab.y + pz * this.ab.z) / this.ab2;
            float tv = this.ac2 == 0.0F ? 0.0F : (px * this.ac.x + py * this.ac.y + pz * this.ac.z) / this.ac2;

            return new float[]{
                    this.au + (this.bu - this.au) * tu + (this.cu - this.au) * tv,
                    this.av + (this.bv - this.av) * tu + (this.cv - this.av) * tv
            };
        }
    }

    private record Corner(Vector3f pos, float u, float v) {
    }

    private CubeFaces(List<Corner[]> quads) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        for (Corner[] quad : quads) {
            for (Corner corner : quad) {
                minX = Math.min(minX, corner.pos.x);
                minY = Math.min(minY, corner.pos.y);
                minZ = Math.min(minZ, corner.pos.z);
                maxX = Math.max(maxX, corner.pos.x);
                maxY = Math.max(maxY, corner.pos.y);
                maxZ = Math.max(maxZ, corner.pos.z);
            }
        }

        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;

        for (Corner[] quad : quads) {
            Direction side = side(quad);

            if (side != null) this.faces[side.ordinal()] = new Face(quad[1], quad[0], quad[2]);
        }
    }

    private Direction side(Corner[] quad) {
        float x = quad[0].pos.x, y = quad[0].pos.y, z = quad[0].pos.z;

        boolean flatX = true, flatY = true, flatZ = true;

        for (Corner corner : quad) {
            flatX &= corner.pos.x == x;
            flatY &= corner.pos.y == y;
            flatZ &= corner.pos.z == z;
        }

        if (flatX) return x == this.minX ? Direction.WEST : Direction.EAST;
        if (flatY) return y == this.minY ? Direction.DOWN : Direction.UP;
        if (flatZ) return z == this.minZ ? Direction.NORTH : Direction.SOUTH;

        return null;
    }

    public float[] uv(Direction side, float x, float y, float z) {
        Face face = this.faces[side.ordinal()];

        return face == null ? new float[]{0.0F, 0.0F} : face.uv(x, y, z);
    }

    public static CubeFaces of(ModelPart part) {
        return CACHE.computeIfAbsent(part, CubeFaces::read);
    }

    private static CubeFaces read(ModelPart part) {
        List<ModelPart.Cube> cubes = new ArrayList<>();

        part.visit(new PoseStack(), (pose, path, index, cube) -> cubes.add(cube));

        Recorder recorder = new Recorder();

        if (!cubes.isEmpty()) {
            cubes.get(0).compile(new PoseStack().last(), recorder, 0, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }

        return new CubeFaces(recorder.quads);
    }

    private static final class Recorder implements VertexConsumer {
        final List<Corner[]> quads = new ArrayList<>();

        private final List<Corner> pending = new ArrayList<>(4);

        private float x, y, z, u, v;

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            this.x = (float) x * 16.0F;
            this.y = (float) y * 16.0F;
            this.z = (float) z * 16.0F;
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            this.u = u;
            this.v = v;
            return this;
        }

        @Override
        public void endVertex() {
            this.pending.add(new Corner(new Vector3f(this.x, this.y, this.z), this.u, this.v));

            if (this.pending.size() == 4) {
                this.quads.add(this.pending.toArray(new Corner[0]));
                this.pending.clear();
            }
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return this;
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
        }

        @Override
        public void unsetDefaultColor() {
        }
    }
}
