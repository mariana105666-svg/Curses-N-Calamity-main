package radon.jujutsu_kaisen.mixin.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {
    @Accessor("buffer")
    ByteBuffer getBuffer();

    @Accessor("nextElementByte")
    int getNextElementByte();

    @Accessor("format")
    VertexFormat getFormat();
}
