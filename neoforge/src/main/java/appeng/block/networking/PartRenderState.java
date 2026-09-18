package appeng.block.networking;

import appeng.api.client.AEModelData;
import appeng.api.parts.IPartItem;

public record PartRenderState(IPartItem<?> partItem, AEModelData modelData) {
}
