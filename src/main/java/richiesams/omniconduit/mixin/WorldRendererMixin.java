package richiesams.omniconduit.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import richiesams.omniconduit.blocks.ConduitBundleBlock;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Accessor
    abstract ClientWorld getWorld();

    @Accessor
    abstract MinecraftClient getClient();

    @Invoker("drawCuboidShapeOutline")
    public static void drawCuboidShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha) {
        throw new AssertionError();
    }

    @Inject(at = @At("HEAD"), method = "drawBlockOutline", cancellable = true)
    private void drawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (state.getBlock() instanceof ConduitBundleBlock conduitBundleBlock) {
            var world = getWorld();
            var client = getClient();

            // Draw our custom outline
            var outline = conduitBundleBlock.getDetailedOutlineShape(client, world, pos);
            WorldRendererMixin.drawCuboidShapeOutline(matrices, vertexConsumer, outline, (double) pos.getX() - cameraX, (double) pos.getY() - cameraY, (double) pos.getZ() - cameraZ, 0.0f, 0.0f, 0.0f, 0.4f);

            // Return early, so the "real" method doesn't also draw an outline
            ci.cancel();
        }
    }
}