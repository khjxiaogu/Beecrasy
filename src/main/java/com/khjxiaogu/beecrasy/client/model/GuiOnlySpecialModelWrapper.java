package com.khjxiaogu.beecrasy.client.model;
import com.google.common.base.Suppliers;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class GuiOnlySpecialModelWrapper<T> implements ItemModel {
    private final SpecialModelRenderer<T> specialRenderer;
    private final ModelRenderProperties properties;
    private final Supplier<Vector3fc[]> extents;
    private final Matrix4fc transformation;

    public GuiOnlySpecialModelWrapper(SpecialModelRenderer<T> specialRenderer, ModelRenderProperties properties, Matrix4fc transformation) {
        this.specialRenderer = specialRenderer;
        this.properties = properties;
        this.extents = Suppliers.memoize(() -> {
            Set<Vector3fc> results = new HashSet<>();
            specialRenderer.getExtents(results::add);
            return results.toArray(new Vector3fc[0]);
        });
        this.transformation = transformation;
    }

    @Override
    public void update(
        ItemStackRenderState output,
        ItemStack item,
        ItemModelResolver resolver,
        ItemDisplayContext displayContext,
        @Nullable ClientLevel level,
        @Nullable ItemOwner owner,
        int seed
    ) {
        output.appendModelIdentityElement(this);
        ItemStackRenderState.LayerRenderState layer = output.newLayer();
        if (item.hasFoil()) {
            ItemStackRenderState.FoilType foilType = ItemStackRenderState.FoilType.STANDARD;
            layer.setFoilType(foilType);
            output.setAnimated();
            output.appendModelIdentityElement(foilType);
        }

        T argument = this.specialRenderer.extractArgument(item);
        layer.setExtents(this.extents);
        layer.setLocalTransform(this.transformation);
        if(displayContext==ItemDisplayContext.GUI)
        	layer.setupSpecialModel(this.specialRenderer, argument);
        if (argument != null) {
            output.appendModelIdentityElement(argument);
        }

        this.properties.applyToLayer(layer, displayContext);
    }

    public record Unbaked(Identifier base, Optional<Transformation> transformation, SpecialModelRenderer.Unbaked<?> specialModel) implements ItemModel.Unbaked {
        public static final MapCodec<GuiOnlySpecialModelWrapper.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    Identifier.CODEC.fieldOf("base").forGetter(GuiOnlySpecialModelWrapper.Unbaked::base),
                    Transformation.EXTENDED_CODEC.optionalFieldOf("transformation").forGetter(GuiOnlySpecialModelWrapper.Unbaked::transformation),
                    SpecialModelRenderers.CODEC.fieldOf("model").forGetter(GuiOnlySpecialModelWrapper.Unbaked::specialModel)
                )
                .apply(i, GuiOnlySpecialModelWrapper.Unbaked::new)
        );

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.base);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            Matrix4fc modelTransform = Transformation.compose(transformation, this.transformation);
            SpecialModelRenderer<?> bakedSpecialModel = this.specialModel.bake(context);
            if (bakedSpecialModel == null) {
                return context.missingItemModel(modelTransform);
            } else {
                ModelRenderProperties properties = this.getProperties(context);
                return new GuiOnlySpecialModelWrapper<>(bakedSpecialModel, properties, modelTransform);
            }
        }

        private ModelRenderProperties getProperties(ItemModel.BakingContext context) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel model = baker.getModel(this.base);
            TextureSlots textureSlots = model.getTopTextureSlots();
            return ModelRenderProperties.fromResolvedModel(baker, model, textureSlots);
        }

        @Override
        public MapCodec<GuiOnlySpecialModelWrapper.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}