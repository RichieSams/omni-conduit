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
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import richiesams.omniconduit.OmniConduitModBase
import richiesams.omniconduit.api.OnmiConduitRegistries
import richiesams.omniconduit.api.blockentities.ConduitBundleBlockEntity
import richiesams.omniconduit.api.conduits.ConduitDisplayMode
import richiesams.omniconduit.api.conduits.ConduitTerminationMode
import richiesams.omniconduit.items.YetaWrenchItem
import richiesams.omniconduit.util.SpriteReference
import richiesams.omniconduit.util.Vec4f

@Environment(EnvType.CLIENT)
class ConduitBundleBlockEntityRenderer(ctx: BlockEntityRendererFactory.Context?) : BlockEntityRenderer<ConduitBundleBlockEntity> {
    companion object {
        private val wireFrameID = Identifier(OmniConduitModBase.MOD_ID, "block/conduit/wire_frame")
        private val terminatorID = Identifier(OmniConduitModBase.MOD_ID, "block/conduit/conduit_terminator")
        private val inArrowID = Identifier(OmniConduitModBase.MOD_ID, "block/conduit/conduit_terminator_in")
        private val outArrowID = Identifier(OmniConduitModBase.MOD_ID, "block/conduit/conduit_terminator_out")
        private val inOutInArrowID = Identifier(OmniConduitModBase.MOD_ID, "block/conduit/conduit_terminator_in_out_in")
        private val inOutOutArrowID = Identifier(OmniConduitModBase.MOD_ID, "block/conduit/conduit_terminator_in_out_out")

    }

    private val sprites: HashMap<Identifier, Sprite> = HashMap()
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

        // Fetch all our misc sprites
        sprites[wireFrameID] = textureGetter.apply(wireFrameID)
        sprites[terminatorID] = textureGetter.apply(terminatorID)
        sprites[inArrowID] = textureGetter.apply(inArrowID)
        sprites[outArrowID] = textureGetter.apply(outArrowID)
        sprites[inOutInArrowID] = textureGetter.apply(inOutInArrowID)
        sprites[inOutOutArrowID] = textureGetter.apply(inOutOutArrowID)
        missingSprite = textureGetter.apply(Identifier(OmniConduitModBase.MOD_ID, "intentionally_missing"))
    }

    private enum class Rotation {
        DEGREES_0,
        DEGREES_90,
        DEGREES_180,
        DEGREES_270
    }

    private class WireFrameCuboidFace(val direction: Direction, val cube: Box, val uvRotation: Rotation) {}

    override fun render(entity: ConduitBundleBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val shape = entity.getRenderShape()

        matrices.push()

        val vertexConsumer: VertexConsumer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout())

        val entry: MatrixStack.Entry = matrices.peek()
        val positionMatrix: Matrix4f = entry.positionMatrix

        val mode = YetaWrenchItem.getCurrentMode(YetaWrenchItem.getEquippedWrench())

        val wireFrameFaces = ArrayList<WireFrameCuboidFace>()

        // First render the cores
        for (core in shape.cores) {
            val solid: Boolean = when (mode) {
                // ALL and CONFIGURE will always display normally
                ConduitDisplayMode.ALL.type -> true
                ConduitDisplayMode.CONFIGURE.type -> true
                // NONE will always display wireframe
                ConduitDisplayMode.NONE.type -> false
                // If we match the mode then we display normally
                core.type -> true
                // If not, then we display wireframe
                else -> false
            }

            if (solid) {
                // Render all the faces
                for (direction in Direction.entries) {
                    renderCuboidFace(
                        vertexConsumer,
                        positionMatrix,
                        direction,
                        core.box,
                        sprites[core.sprite.identifier] ?: missingSprite,
                        core.sprite.uvFrom,
                        core.sprite.uvTo,
                        Rotation.DEGREES_0
                    )
                }
            } else {
                for (direction in Direction.entries) {
                    // Save the wireframe faces to render later.
                    // To give transparency the best chance
                    wireFrameFaces.add(
                        WireFrameCuboidFace(
                            direction,
                            core.box,
                            Rotation.DEGREES_0
                        )
                    )
                }
            }
        }

        // Then render the connectors
        for (connection in shape.connections) {
            val solid: Boolean = when (mode) {
                // ALL and CONFIGURE will always display normally
                ConduitDisplayMode.ALL.type -> true
                ConduitDisplayMode.CONFIGURE.type -> true
                // NONE will always display wireframe
                ConduitDisplayMode.NONE.type -> false
                // If we match the mode then we display normally
                connection.type -> true
                // If not, then we display wireframe
                else -> false
            }

            if (solid) {
                // Render the inner texture first, if it exists
                val connectorSpriteRefs: ArrayList<SpriteReference> = ArrayList<SpriteReference>()
                if (connection.innerSprite != null) {
                    connectorSpriteRefs.add(connection.innerSprite)
                }
                connectorSpriteRefs.add(connection.outerSprite)

                for (spriteRef in connectorSpriteRefs) {
                    val sprite = sprites[spriteRef.identifier] ?: missingSprite

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
            } else {
                val faces: Array<Direction>
                when (connection.direction) {
                    Direction.UP -> {
                        faces = arrayOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
                    }

                    Direction.DOWN -> {
                        faces = arrayOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
                    }

                    Direction.NORTH -> {
                        faces = arrayOf(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST)
                    }

                    Direction.SOUTH -> {
                        faces = arrayOf(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST)
                    }

                    Direction.EAST -> {
                        faces = arrayOf(Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH)
                    }

                    Direction.WEST -> {
                        faces = arrayOf(Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH)
                    }
                }

                for (face in faces) {
                    wireFrameFaces.add(
                        WireFrameCuboidFace(
                            face,
                            connection.box,
                            Rotation.DEGREES_0
                        )
                    )
                }
            }
        }

        // Then render the terminators
        for (termination in shape.terminations) {
            val solid: Boolean = when (mode) {
                // ALL and CONFIGURE will always display normally
                ConduitDisplayMode.ALL.type -> true
                ConduitDisplayMode.CONFIGURE.type -> true
                // NONE will always display wireframe
                ConduitDisplayMode.NONE.type -> false
                // If we match the mode then we display normally
                termination.type -> true
                // If not, then we display wireframe
                else -> false
            }

            if (solid) {
                val sprite = sprites[terminatorID] ?: missingSprite

                val faceUVFrom = Vec2f(0.0f, 0.0f)
                val faceUVTo = Vec2f(1.0f, 1.0f)
                val edgeUVFrom = Vec2f(0.4f, 0.6f)
                val edgeUVTo = Vec2f(0.4f, 0.6f)

                // Render the outer box
                kotlin.run {
                    // We need all the faces for this
                    val edges: Array<Direction>
                    val faces: Array<Direction>
                    when (termination.direction) {
                        Direction.UP -> {
                            edges = arrayOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
                            faces = arrayOf(Direction.UP, Direction.DOWN)
                        }

                        Direction.DOWN -> {
                            edges = arrayOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
                            faces = arrayOf(Direction.UP, Direction.DOWN)
                        }

                        Direction.NORTH -> {
                            edges = arrayOf(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST)
                            faces = arrayOf(Direction.NORTH, Direction.SOUTH)
                        }

                        Direction.SOUTH -> {
                            edges = arrayOf(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST)
                            faces = arrayOf(Direction.NORTH, Direction.SOUTH)
                        }

                        Direction.EAST -> {
                            edges = arrayOf(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST)
                            faces = arrayOf(Direction.NORTH, Direction.SOUTH)
                        }

                        Direction.WEST -> {
                            edges = arrayOf(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST)
                            faces = arrayOf(Direction.NORTH, Direction.SOUTH)
                        }
                    }

                    for (direction in edges) {
                        renderCuboidFace(
                            vertexConsumer,
                            positionMatrix,
                            direction,
                            termination.outerBox,
                            sprite,
                            edgeUVFrom,
                            edgeUVTo,
                            Rotation.DEGREES_0
                        )
                    }
                    for (direction in faces) {
                        renderCuboidFace(
                            vertexConsumer,
                            positionMatrix,
                            direction,
                            termination.outerBox,
                            sprite,
                            faceUVFrom,
                            faceUVTo,
                            Rotation.DEGREES_0
                        )
                    }
                }

                // Render the inner box
                kotlin.run {
                    // We can leave off the face next to the outer box
                    val edges: Array<Direction>
                    val face: Direction
                    when (termination.direction) {
                        Direction.UP -> {
                            edges = arrayOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
                            face = Direction.DOWN
                        }

                        Direction.DOWN -> {
                            edges = arrayOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
                            face = Direction.UP
                        }

                        Direction.NORTH -> {
                            edges = arrayOf(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST)
                            face = Direction.SOUTH
                        }

                        Direction.SOUTH -> {
                            edges = arrayOf(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST)
                            face = Direction.NORTH
                        }

                        Direction.EAST -> {
                            edges = arrayOf(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST)
                            face = Direction.WEST
                        }

                        Direction.WEST -> {
                            edges = arrayOf(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST)
                            face = Direction.EAST
                        }
                    }

                    for (direction in edges) {
                        renderCuboidFace(
                            vertexConsumer,
                            positionMatrix,
                            direction,
                            termination.innerBox,
                            sprite,
                            edgeUVFrom,
                            edgeUVTo,
                            Rotation.DEGREES_0
                        )
                    }
                    renderCuboidFace(
                        vertexConsumer,
                        positionMatrix,
                        face,
                        termination.innerBox,
                        sprite,
                        faceUVFrom,
                        faceUVTo,
                        Rotation.DEGREES_0
                    )
                }

                // Render the io connector
                kotlin.run {
                    // We can leave off the face next to the inner box
                    val edges: Array<Direction>
                    val face: Direction
                    when (termination.direction) {
                        Direction.UP -> {
                            edges = arrayOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
                            face = Direction.DOWN
                        }

                        Direction.DOWN -> {
                            edges = arrayOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
                            face = Direction.UP
                        }

                        Direction.NORTH -> {
                            edges = arrayOf(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST)
                            face = Direction.SOUTH
                        }

                        Direction.SOUTH -> {
                            edges = arrayOf(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST)
                            face = Direction.NORTH
                        }

                        Direction.EAST -> {
                            edges = arrayOf(Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH)
                            face = Direction.WEST
                        }

                        Direction.WEST -> {
                            edges = arrayOf(Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH)
                            face = Direction.EAST
                        }
                    }

                    for (direction in edges) {
                        renderCuboidFace(
                            vertexConsumer,
                            positionMatrix,
                            direction,
                            termination.ioConnectorBox,
                            sprite,
                            edgeUVFrom,
                            edgeUVTo,
                            Rotation.DEGREES_0
                        )
                    }
                    renderCuboidFace(
                        vertexConsumer,
                        positionMatrix,
                        face,
                        termination.ioConnectorBox,
                        sprite,
                        faceUVFrom,
                        faceUVTo,
                        Rotation.DEGREES_0
                    )
                }

                // Render the io connector arrows
                kotlin.run {
                    val ioArrowSprites: Array<Pair<Sprite, DyeColor>>
                    when (termination.terminationMode) {
                        ConduitTerminationMode.NONE -> {
                            throw RuntimeException("Conduit Terminator with NONE type")
                        }

                        ConduitTerminationMode.INPUT_OUTPUT -> {
                            ioArrowSprites = arrayOf(
                                Pair(sprites[inOutInArrowID] ?: missingSprite, DyeColor.RED),
                                Pair(sprites[inOutOutArrowID] ?: missingSprite, DyeColor.RED),
                            )
                        }

                        ConduitTerminationMode.INPUT_ONLY -> {
                            ioArrowSprites = arrayOf(Pair(sprites[inArrowID] ?: missingSprite, DyeColor.RED))

                        }

                        ConduitTerminationMode.OUTPUT_ONLY -> {
                            ioArrowSprites = arrayOf(Pair(sprites[outArrowID] ?: missingSprite, DyeColor.RED))
                        }
                    }

                    val uvFrom = Vec2f(0.0f, 0.0f)
                    val uvTo = Vec2f(1.0f, 1.0f)

                    for (pair in ioArrowSprites) {
                        val ioArrowSprite = pair.first
                        val dyeColor = pair.second
                        val colorComponents = dyeColor.colorComponents
                        val color = Vec4f(colorComponents[0], colorComponents[1], colorComponents[2], 1.0f)

                        when (termination.direction) {
                            Direction.UP -> {
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.NORTH,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_270,
                                    color,
                                )
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.SOUTH,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_270,
                                    color,
                                )
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.EAST,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_270,
                                    color,
                                )
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.WEST,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_270,
                                    color,
                                )
                            }

                            Direction.DOWN -> {
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.NORTH,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_90,
                                    color,
                                )
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.SOUTH,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_90,
                                    color,
                                )
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.EAST,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_90,
                                    color,
                                )
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.WEST,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_90,
                                    color,
                                )
                            }

                            Direction.NORTH -> {
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.EAST,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_0,
                                    color,
                                )
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.WEST,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_180,
                                    color,
                                )
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.UP,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_90,
                                    color,
                                )
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.DOWN,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_270,
                                    color,
                                )
                            }

                            Direction.SOUTH -> {
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.EAST,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_180,
                                    color,
                                )
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.WEST,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_0,
                                    color,
                                )
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.UP,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_270,
                                    color,
                                )
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.DOWN,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_90,
                                    color,
                                )
                            }

                            Direction.EAST -> {
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.NORTH,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_180,
                                    color,
                                )
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.SOUTH,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_0,
                                    color,
                                )
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.UP,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_180,
                                    color,
                                )
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.DOWN,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_0,
                                    color,
                                )
                            }

                            Direction.WEST -> {
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.NORTH,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_0,
                                    color,
                                )
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.SOUTH,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_180,
                                    color,
                                )
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.UP,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_0,
                                    color,
                                )
                                renderCuboidFace(
                                    vertexConsumer,
                                    positionMatrix,
                                    Direction.DOWN,
                                    termination.ioConnectorBox,
                                    ioArrowSprite,
                                    uvFrom,
                                    uvTo,
                                    Rotation.DEGREES_180,
                                    color,
                                )
                            }
                        }
                    }
                }
            } else {
                // Render the outer box
                for (direction in Direction.entries) {
                    wireFrameFaces.add(
                        WireFrameCuboidFace(
                            direction,
                            termination.outerBox,
                            Rotation.DEGREES_0
                        )
                    )
                }

                // Render the inner box
                for (direction in Direction.entries) {
                    wireFrameFaces.add(
                        WireFrameCuboidFace(
                            direction,
                            termination.innerBox,
                            Rotation.DEGREES_0
                        )
                    )
                }

                // Render the io connector
                for (direction in Direction.entries) {
                    wireFrameFaces.add(
                        WireFrameCuboidFace(
                            direction,
                            termination.ioConnectorBox,
                            Rotation.DEGREES_0
                        )
                    )
                }
            }
        }

        // Now we render all the wireframe faces
        val sprite = sprites[wireFrameID] ?: missingSprite
        for (face in wireFrameFaces) {
            renderCuboidFace(vertexConsumer, positionMatrix, face.direction, face.cube, sprite, Vec2f(0.0f, 0.0f), Vec2f(1.0f, 1.0f), face.uvRotation)
        }

        matrices.pop()
    }

    private fun renderCuboidFace(
        vertexConsumer: VertexConsumer, positionMatrix: Matrix4f,
        direction: Direction,
        cube: Box,
        sprite: Sprite, uvFrom: Vec2f, uvTo: Vec2f, uvRotation: Rotation,
        color: Vec4f = Vec4f(1.0f, 1.0f, 1.0f, 1.0f)
    ) {
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
                    uvRotation,
                    color,
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
                    uvRotation,
                    color,
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
                    uvRotation,
                    color,
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
                    uvRotation,
                    color,
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
                    uvRotation,
                    color,
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
                    uvRotation,
                    color,
                )
            }
        }
    }

    private fun renderQuad(
        vertexConsumer: VertexConsumer, positionMatrix: Matrix4f, nominalDirection: Direction,
        vertex0: Vec3d, vertex1: Vec3d, vertex2: Vec3d, vertex3: Vec3d,
        sprite: Sprite, uvFrom: Vec2f, uvTo: Vec2f, uvRotation: Rotation,
        color: Vec4f = Vec4f(1.0f, 1.0f, 1.0f, 1.0f)
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
            .color(color.x, color.y, color.z, color.w)
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
            .color(color.x, color.y, color.z, color.w)
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
            .color(color.x, color.y, color.z, color.w)
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
            .color(color.x, color.y, color.z, color.w)
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
