package com.example.smoothdoors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(SmoothDoors.MOD_ID)
public class SmoothDoors {
    public static final String MOD_ID = "smoothdoors";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);

    public static final Map<String, DeferredBlock<SmoothDoorBlock>> DOORS = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<Item>> DOOR_ITEMS = new LinkedHashMap<>();

    static {
        door("oak", () -> Blocks.OAK_DOOR, BlockSetType.OAK);
        door("spruce", () -> Blocks.SPRUCE_DOOR, BlockSetType.SPRUCE);
        door("birch", () -> Blocks.BIRCH_DOOR, BlockSetType.BIRCH);
        door("jungle", () -> Blocks.JUNGLE_DOOR, BlockSetType.JUNGLE);
        door("acacia", () -> Blocks.ACACIA_DOOR, BlockSetType.ACACIA);
        door("dark_oak", () -> Blocks.DARK_OAK_DOOR, BlockSetType.DARK_OAK);
        door("mangrove", () -> Blocks.MANGROVE_DOOR, BlockSetType.MANGROVE);
        door("cherry", () -> Blocks.CHERRY_DOOR, BlockSetType.CHERRY);
        door("bamboo", () -> Blocks.BAMBOO_DOOR, BlockSetType.BAMBOO);
        door("crimson", () -> Blocks.CRIMSON_DOOR, BlockSetType.CRIMSON);
        door("warped", () -> Blocks.WARPED_DOOR, BlockSetType.WARPED);
        door("iron", () -> Blocks.IRON_DOOR, BlockSetType.IRON);
    }

    public static final Supplier<BlockEntityType<SmoothDoorBlockEntity>> DOOR_BE =
            BLOCK_ENTITIES.register("smooth_door", () -> BlockEntityType.Builder.of(
                    SmoothDoorBlockEntity::new,
                    DOORS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));

    private static void door(String name, Supplier<Block> base, BlockSetType type) {
        String id = "smooth_" + name + "_door";
        DeferredBlock<SmoothDoorBlock> block = BLOCKS.register(id,
                () -> new SmoothDoorBlock(type, BlockBehaviour.Properties.ofFullCopy(base.get())));
        DOORS.put(name, block);
        DOOR_ITEMS.put(name, ITEMS.register(id,
                () -> new DoubleHighBlockItem(block.get(), new Item.Properties())));
    }

    public SmoothDoors(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS
                || event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            DOOR_ITEMS.values().forEach(i -> event.accept(i.get()));
        }
    }
}
