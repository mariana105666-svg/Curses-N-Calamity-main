package radon.jujutsu_kaisen.client.puppet;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

public final class Highlight {
    public static final float FILL = 0.22F;
    public static final float EDGE = 0.95F;

    private Highlight() {
    }

    public record Region(double x1, double y1, double z1, double x2, double y2, double z2,
                         float red, float green, float blue) {
        public static Region of(BlockPos pos, float red, float green, float blue) {
            return new Region(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1.0D, pos.getY() + 1.0D, pos.getZ() + 1.0D, red, green, blue);
        }

        public static Region of(BlockPos min, BlockPos max, float red, float green, float blue) {
            return new Region(
                    Math.min(min.getX(), max.getX()), Math.min(min.getY(), max.getY()), Math.min(min.getZ(), max.getZ()),
                    Math.max(min.getX(), max.getX()) + 1.0D, Math.max(min.getY(), max.getY()) + 1.0D,
                    Math.max(min.getZ(), max.getZ()) + 1.0D, red, green, blue);
        }
    }

    public static void render(PoseStack pose, Vec3 camera, List<Region> regions) {
        if (regions.isEmpty()) return;

        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);

        Matrix4f matrix = pose.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (Region region : regions) faces(buffer, matrix, region);

        tesselator.end();

        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        for (Region region : regions) outline(buffer, matrix, region);

        tesselator.end();

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        pose.popPose();
    }

    private static void faces(BufferBuilder buffer, Matrix4f matrix, Region r) {
        quad(buffer, matrix, r, r.x1, r.y1, r.z1, r.x2, r.y1, r.z1, r.x2, r.y2, r.z1, r.x1, r.y2, r.z1);
        quad(buffer, matrix, r, r.x1, r.y1, r.z2, r.x2, r.y1, r.z2, r.x2, r.y2, r.z2, r.x1, r.y2, r.z2);
        quad(buffer, matrix, r, r.x1, r.y1, r.z1, r.x1, r.y1, r.z2, r.x1, r.y2, r.z2, r.x1, r.y2, r.z1);
        quad(buffer, matrix, r, r.x2, r.y1, r.z1, r.x2, r.y1, r.z2, r.x2, r.y2, r.z2, r.x2, r.y2, r.z1);
        quad(buffer, matrix, r, r.x1, r.y1, r.z1, r.x2, r.y1, r.z1, r.x2, r.y1, r.z2, r.x1, r.y1, r.z2);
        quad(buffer, matrix, r, r.x1, r.y2, r.z1, r.x2, r.y2, r.z1, r.x2, r.y2, r.z2, r.x1, r.y2, r.z2);
    }

    private static void quad(BufferBuilder buffer, Matrix4f matrix, Region r,
                             double ax, double ay, double az, double bx, double by, double bz,
                             double cx, double cy, double cz, double dx, double dy, double dz) {
        vertex(buffer, matrix, r, ax, ay, az, FILL);
        vertex(buffer, matrix, r, bx, by, bz, FILL);
        vertex(buffer, matrix, r, cx, cy, cz, FILL);
        vertex(buffer, matrix, r, dx, dy, dz, FILL);
    }

    private static void outline(BufferBuilder buffer, Matrix4f matrix, Region r) {
        line(buffer, matrix, r, r.x1, r.y1, r.z1, r.x2, r.y1, r.z1);
        line(buffer, matrix, r, r.x2, r.y1, r.z1, r.x2, r.y1, r.z2);
        line(buffer, matrix, r, r.x2, r.y1, r.z2, r.x1, r.y1, r.z2);
        line(buffer, matrix, r, r.x1, r.y1, r.z2, r.x1, r.y1, r.z1);

        line(buffer, matrix, r, r.x1, r.y2, r.z1, r.x2, r.y2, r.z1);
        line(buffer, matrix, r, r.x2, r.y2, r.z1, r.x2, r.y2, r.z2);
        line(buffer, matrix, r, r.x2, r.y2, r.z2, r.x1, r.y2, r.z2);
        line(buffer, matrix, r, r.x1, r.y2, r.z2, r.x1, r.y2, r.z1);

        line(buffer, matrix, r, r.x1, r.y1, r.z1, r.x1, r.y2, r.z1);
        line(buffer, matrix, r, r.x2, r.y1, r.z1, r.x2, r.y2, r.z1);
        line(buffer, matrix, r, r.x2, r.y1, r.z2, r.x2, r.y2, r.z2);
        line(buffer, matrix, r, r.x1, r.y1, r.z2, r.x1, r.y2, r.z2);
    }

    private static void line(BufferBuilder buffer, Matrix4f matrix, Region r,
                             double x1, double y1, double z1, double x2, double y2, double z2) {
        vertex(buffer, matrix, r, x1, y1, z1, EDGE);
        vertex(buffer, matrix, r, x2, y2, z2, EDGE);
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, Region r,
                               double x, double y, double z, float alpha) {
        buffer.vertex(matrix, (float) x, (float) y, (float) z)
                .color(r.red, r.green, r.blue, alpha).endVertex();
    }
}
