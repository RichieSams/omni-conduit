package richiesams.omniconduit.rendering

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.TexturedRenderLayers
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.Items
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import richiesams.omniconduit.OmniConduitModBase
import richiesams.omniconduit.api.OnmiConduitRegistries
import richiesams.omniconduit.blockentities.ConduitBundleBlockEntity
import richiesams.omniconduit.util.SpriteReference

@Environment(EnvType.CLIENT)
class ConduitBundleBlockEntityRenderer(ctx: BlockEntityRendererFactory.Context?) : BlockEntityRenderer<ConduitBundleBlockEntity> {
    private val sprites: HashMap<Identifier, Sprite> = HashMap<Identifier, Sprite>()
    private val wireFrame: Sprite
    private val missingSprite: Sprite

    init {
        val textureGetter = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)

        // Fetch all the sprites
        for (entry in OnmiConduitRegistries.CONDUIT.entrySet) {
            val coreID: Identifier = entry.value.coreSprite.identifier
            val coreSprite: Sprite = textureGetter.apply(coreID)
            sprites[coreID] = coreSprite

            val connectorOuterID: Identifier = entry.value.connectorOuterSprite.identifier
            val connectorOuterSprite: Sprite = textureGetter.apply(connectorOuterID)
            sprites[connectorOuterID] = connectorOuterSprite

            if (entry.value.connectorInnerSprite != null) {
                val connectorInnerID: Identifier = entry.value.connectorInnerSprite!!.identifier
                val connectorInnerSprite: Sprite = textureGetter.apply(connectorInnerID)
                sprites[connectorInnerID] = connectorInnerSprite
            }
        }

        wireFrame = textureGetter.apply(Identifier(OmniConduitModBase.MOD_ID, "block/conduit/wire_frame"))
        missingSprite = textureGetter.apply(Identifier(OmniConduitModBase.MOD_ID, "intentionally_missing"))
    }

    enum class Rotation {
        DEGREES_0,
        DEGREES_90,
        DEGREES_180,
        DEGREES_270
    }

    override fun render(entity: ConduitBundleBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val shape = entity.getConduitShape()

        matrices.push()

        val vertexConsumer: VertexConsumer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout())

        val entry: MatrixStack.Entry = matrices.peek()
        val positionMatrix: Matrix4f = entry.positionMatrix

        val player = MinecraftClient.getInstance().player!!
        val stack = player.getStackInHand(player.activeHand)
        if (stack.isOf(Items.GRASS_BLOCK)) {
            // Render all the faces
            for (direction in Direction.entries) {
                renderCuboidFace(
                    vertexConsumer,
                    positionMatrix,
                    direction,
                    Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0),
                    wireFrame,
                    Vec2f(0.0f, 0.0f),
                    Vec2f(1.0f, 1.0f),
                    Rotation.DEGREES_0
                )
            }
        } else {

            // First render the cores
            for (core in shape.cores) {
                renderCuboid(vertexConsumer, sprites, positionMatrix, core.box, core.sprite)
            }

            // Then render the connectors
            for (connection in shape.connections) {
                // Render the inner texture first, if it exists
                val connectorSpriteRefs: ArrayList<SpriteReference> = ArrayList<SpriteReference>()
                if (connection.innerSprite != null) {
                    connectorSpriteRefs.add(connection.innerSprite)
                }
                connectorSpriteRefs.add(connection.outerSprite)

                for (spriteRef in connectorSpriteRefs) {
                    val sprite: Sprite = sprites[spriteRef.identifier] ?: missingSprite

                    when (connection.direction) {
                        Direction.UP -> {
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.NORTH,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_270
                            )
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.SOUTH,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_270
                            )
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.EAST,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_270
                            )
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.WEST,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_270
                            )
                        }

                        Direction.DOWN -> {
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.NORTH,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_90
                            )
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.SOUTH,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_90
                            )
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.EAST,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_90
                            )
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.WEST,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_90
                            )
                        }

                        Direction.NORTH -> {
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.EAST,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_0
                            )
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.WEST,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_180
                            )
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.UP,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_90
                            )
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.DOWN,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_270
                            )
                        }

                        Direction.SOUTH -> {
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.EAST,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_180
                            )
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.WEST,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_0
                            )
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.UP,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_270
                            )
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.DOWN,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_90
                            )
                        }

                        Direction.EAST -> {
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.NORTH,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_180
                            )
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.SOUTH,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_0
                            )
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.UP,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_180
                            )
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.DOWN,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_0
                            )
                        }

                        Direction.WEST -> {
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.NORTH,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_0
                            )
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.SOUTH,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_180
                            )
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.UP,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_0
                            )
                            renderCuboidFace(
                                vertexConsumer,
                                positionMatrix,
                                Direction.DOWN,
                                connection.box,
                                sprite,
                                spriteRef.uvFrom,
                                spriteRef.uvTo,
                                Rotation.DEGREES_180
                            )
                        }
                    }
                }
            }
        }

        matrices.pop()
    }


    private fun renderCuboid(vertexConsumer: VertexConsumer, spriteMap: Map<Identifier, Sprite>, positionMatrix: Matrix4f, cube: Box, spriteRef: SpriteReference) {
        val sprite: Sprite = spriteMap[spriteRef.identifier] ?: missingSprite

        // Render all the faces
        for (direction in Direction.entries) {
            renderCuboidFace(
                vertexConsumer,
                positionMatrix,
                direction,
                cube,
                sprite,
                spriteRef.uvFrom,
                spriteRef.uvTo,
                Rotation.DEGREES_0
            )
        }
    }


    private fun renderCuboidFace(vertexConsumer: VertexConsumer, positionMatrix: Matrix4f, direction: Direction, cube: Box, sprite: Sprite, uvFrom: Vec2f, uvTo: Vec2f, uvRotation: Rotation) {
        when (direction) {
            Direction.DOWN -> {
                renderQuad(
                    vertexConsumer, positionMatrix, Direction.DOWN,
                    Vec3d(cube.maxX, cube.minY, cube.minZ),
                    Vec3d(cube.maxX, cube.minY, cube.maxZ),
                    Vec3d(cube.minX, cube.minY, cube.maxZ),
                    Vec3d(cube.minX, cube.minY, cube.minZ),
                    sprite,
                    uvFrom,
                    uvTo,
                    uvRotation
                )
            }

            Direction.UP -> {
                renderQuad(
                    vertexConsumer, positionMatrix, Direction.UP,
                    Vec3d(cube.maxX, cube.maxY, cube.maxZ),
                    Vec3d(cube.maxX, cube.maxY, cube.minZ),
                    Vec3d(cube.minX, cube.maxY, cube.minZ),
                    Vec3d(cube.minX, cube.maxY, cube.maxZ),
                    sprite,
                    uvFrom,
                    uvTo,
                    uvRotation
                )
            }

            Direction.NORTH -> {
                renderQuad(
                    vertexConsumer, positionMatrix, Direction.NORTH,
                    Vec3d(cube.maxX, cube.maxY, cube.minZ),
                    Vec3d(cube.maxX, cube.minY, cube.minZ),
                    Vec3d(cube.minX, cube.minY, cube.minZ),
                    Vec3d(cube.minX, cube.maxY, cube.minZ),
                    sprite,
                    uvFrom,
                    uvTo,
                    uvRotation
                )
            }

            Direction.SOUTH -> {
                renderQuad(
                    vertexConsumer, positionMatrix, Direction.SOUTH,
                    Vec3d(cube.minX, cube.maxY, cube.maxZ),
                    Vec3d(cube.minX, cube.minY, cube.maxZ),
                    Vec3d(cube.maxX, cube.minY, cube.maxZ),
                    Vec3d(cube.maxX, cube.maxY, cube.maxZ),
                    sprite,
                    uvFrom,
                    uvTo,
                    uvRotation
                )
            }

            Direction.WEST -> {
                renderQuad(
                    vertexConsumer, positionMatrix, Direction.WEST,
                    Vec3d(cube.minX, cube.maxY, cube.minZ),
                    Vec3d(cube.minX, cube.minY, cube.minZ),
                    Vec3d(cube.minX, cube.minY, cube.maxZ),
                    Vec3d(cube.minX, cube.maxY, cube.maxZ),
                    sprite,
                    uvFrom,
                    uvTo,
                    uvRotation
                )
            }

            Direction.EAST -> {
                renderQuad(
                    vertexConsumer, positionMatrix, Direction.EAST,
                    Vec3d(cube.maxX, cube.maxY, cube.maxZ),
                    Vec3d(cube.maxX, cube.minY, cube.maxZ),
                    Vec3d(cube.maxX, cube.minY, cube.minZ),
                    Vec3d(cube.maxX, cube.maxY, cube.minZ),
                    sprite,
                    uvFrom,
                    uvTo,
                    uvRotation
                )
            }
        }
    }

    private fun renderQuad(
        vertexConsumer: VertexConsumer, positionMatrix: Matrix4f, nominalDirection: Direction,
        vertex0: Vec3d, vertex1: Vec3d, vertex2: Vec3d, vertex3: Vec3d,
        sprite: Sprite, uvFrom: Vec2f, uvTo: Vec2f, uvRotation: Rotation
    ) {
        // Shift the UV range to fit within the range of the sprite within the atlas
        val uMin: Float = sprite.minU
        val uSpan: Float = sprite.maxU - uMin
        val vMin: Float = sprite.minV
        val vSpan: Float = sprite.maxV - vMin

        // Vertex 0
        val uv0 = getSpriteUV(0, uvFrom, uvTo, uvRotation)
        vertexConsumer.vertex(
            positionMatrix,
            vertex0.getX().toFloat(),
            vertex0.getY().toFloat(),
            vertex0.getZ().toFloat()
        )
            .color(1.0f, 1.0f, 1.0f, 1.0f)
            .texture(uMin + uv0.x * uSpan, vMin + uv0.y * vSpan)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(15728880)
            .normal(
                nominalDirection.offsetX.toFloat(),
                nominalDirection.offsetY.toFloat(),
                nominalDirection.offsetZ.toFloat()
            )
            .next()

        // Vertex 1
        val uv1 = getSpriteUV(1, uvFrom, uvTo, uvRotation)
        vertexConsumer.vertex(
            positionMatrix,
            vertex1.getX().toFloat(),
            vertex1.getY().toFloat(),
            vertex1.getZ().toFloat()
        )
            .color(1.0f, 1.0f, 1.0f, 1.0f)
            .texture(uMin + uv1.x * uSpan, vMin + uv1.y * vSpan)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(15728880)
            .normal(
                nominalDirection.offsetX.toFloat(),
                nominalDirection.offsetY.toFloat(),
                nominalDirection.offsetZ.toFloat()
            )
            .next()

        // Vertex 2
        val uv2 = getSpriteUV(2, uvFrom, uvTo, uvRotation)
        vertexConsumer.vertex(
            positionMatrix,
            vertex2.getX().toFloat(),
            vertex2.getY().toFloat(),
            vertex2.getZ().toFloat()
        )
            .color(1.0f, 1.0f, 1.0f, 1.0f)
            .texture(uMin + uv2.x * uSpan, vMin + uv2.y * vSpan)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(15728880)
            .normal(
                nominalDirection.offsetX.toFloat(),
                nominalDirection.offsetY.toFloat(),
                nominalDirection.offsetZ.toFloat()
            )
            .next()

        // Vertex 3
        val uv3 = getSpriteUV(3, uvFrom, uvTo, uvRotation)
        vertexConsumer.vertex(
            positionMatrix,
            vertex3.getX().toFloat(),
            vertex3.getY().toFloat(),
            vertex3.getZ().toFloat()
        )
            .color(1.0f, 1.0f, 1.0f, 1.0f)
            .texture(uMin + uv3.x * uSpan, vMin + uv3.y * vSpan)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(15728880)
            .normal(
                nominalDirection.offsetX.toFloat(),
                nominalDirection.offsetY.toFloat(),
                nominalDirection.offsetZ.toFloat()
            )
            .next()
    }

    private fun getSpriteUV(vertex: Int, uvFrom: Vec2f, uvTo: Vec2f, rotation: Rotation): Vec2f {
        val uv0 = Vec2f(uvFrom.x, uvFrom.y)
        val uv1 = Vec2f(uvFrom.x, uvTo.y)
        val uv2 = Vec2f(uvTo.x, uvTo.y)
        val uv3 = Vec2f(uvTo.x, uvFrom.y)

        return when (rotation) {
            Rotation.DEGREES_0 -> {
                when (vertex) {
                    0 -> uv0
                    1 -> uv1
                    2 -> uv2
                    3 -> uv3
                    else -> throw RuntimeException("Invalid vertex index")
                }
            }

            Rotation.DEGREES_90 -> {
                when (vertex) {
                    0 -> {
                        Vec2f(uv1.x, uv1.y)
                    }

                    1 -> {
                        Vec2f(uv2.x, uv2.y)
                    }

                    2 -> {
                        Vec2f(uv3.x, uv3.y)
                    }

                    3 -> {
                        Vec2f(uv0.x, uv0.y)
                    }

                    else -> throw RuntimeException("Invalid vertex index")
                }
            }

            Rotation.DEGREES_180 -> {
                when (vertex) {
                    0 -> {
                        Vec2f(uv2.x, uv2.y)
                    }

                    1 -> {
                        Vec2f(uv3.x, uv3.y)
                    }

                    2 -> {
                        Vec2f(uv0.x, uv0.y)
                    }

                    3 -> {
                        Vec2f(uv1.x, uv1.y)
                    }

                    else -> throw RuntimeException("Invalid vertex index")
                }
            }

            Rotation.DEGREES_270 -> {
                when (vertex) {
                    0 -> {
                        Vec2f(uv3.x, uv3.y)
                    }

                    1 -> {
                        Vec2f(uv0.x, uv0.y)
                    }

                    2 -> {
                        Vec2f(uv1.x, uv1.y)
                    }

                    3 -> {
                        Vec2f(uv2.x, uv2.y)
                    }

                    else -> throw RuntimeException("Invalid vertex index")
                }
            }
        }
    }
}
