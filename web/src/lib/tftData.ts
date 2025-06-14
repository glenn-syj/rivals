import type {
  TftChampion,
  TftItem,
  TftTrait,
  TftData,
} from "@/lib/cdragon_types/tft";
import tftData from "@/data/sets-modified.json";

// Efficient caching using Maps for O(1) lookup
const championCache = new Map<string, TftChampion>();
const itemCache = new Map<string, TftItem>();
const traitCache = new Map<string, TftTrait>();

// Initialize cache on module load
function initializeCache() {
  const data = tftData as TftData;

  // Cache all items
  data.items.forEach((item) => {
    itemCache.set(item.apiName, item);
  });

  // Cache champions and traits from all sets
  Object.values(data.sets).forEach((setData) => {
    setData.champions.forEach((champion) => {
      championCache.set(champion.apiName, champion);
    });

    setData.traits.forEach((trait) => {
      traitCache.set(trait.apiName, trait);
    });
  });
}

// Initialize cache immediately
initializeCache();

export function getChampionData(championId: string): TftChampion | undefined {
  return championCache.get(championId);
}

export function getItemData(itemId: string): TftItem | undefined {
  return itemCache.get(itemId);
}

export function getTraitData(traitId: string): TftTrait | undefined {
  return traitCache.get(traitId);
}

// Batch get functions for better performance
export function getMultipleItemsData(
  itemIds: string[]
): (TftItem | undefined)[] {
  return itemIds.map((id) => itemCache.get(id));
}

export function getMultipleTraitsData(
  traitIds: string[]
): (TftTrait | undefined)[] {
  return traitIds.map((id) => traitCache.get(id));
}

// Current set version
export const CURRENT_SET = "14";
