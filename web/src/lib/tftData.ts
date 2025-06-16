import type {
  TftChampion,
  TftItem,
  TftTrait,
  TftData,
} from "@/lib/cdragon_types/tft";
import tftData from "@/data/sets-modified.json";
import itemsData from "@/data/items-modified.json";
import traitsData from "@/data/traits-modified.json";

// Efficient caching using Maps for O(1) lookup
const championCache = new Map<string, TftChampion>();
const itemCache = new Map<string, TftItem>();
const traitCache = new Map<string, TftTrait>();

interface TraitData {
  apiName: string;
  desc: string;
  modifiedDesc: string;
}

interface SetData {
  traits: TraitData[];
}

interface TraitsModifiedData {
  sets: {
    [key: string]: SetData;
  };
}

// Initialize cache on module load
function initializeCache() {
  // 타입 단언을 수정하여 안전하게 처리
  const rawData = tftData as unknown as {
    sets: {
      [key: string]: {
        champions: Array<{
          apiName: string;
          name: string;
          ability?: {
            desc: string;
            icon: string;
            name: string;
            modifiedDesc?: string;
          };
        }>;
      };
    };
  };

  // Cache champions
  Object.values(rawData.sets).forEach((setData) => {
    setData.champions.forEach((champion) => {
      const tftChampion: TftChampion = {
        apiName: champion.apiName,
        name: champion.name,
        ability: champion.ability
          ? {
              modifiedDesc:
                champion.ability.modifiedDesc || champion.ability.desc,
            }
          : undefined,
      };
      championCache.set(champion.apiName, tftChampion);
    });
  });

  // Cache items
  itemsData.forEach((item: any) => {
    const tftItem: TftItem = {
      apiName: item.apiName,
      name: item.name,
      modifiedDesc: item.modifiedDesc || item.desc,
    };
    itemCache.set(item.apiName, tftItem);
  });

  // Cache traits
  const typedTraitsData = traitsData as TraitsModifiedData;
  Object.values(typedTraitsData.sets).forEach((setData) => {
    setData.traits.forEach((trait) => {
      const tftTrait: TftTrait = {
        apiName: trait.apiName,
        name: trait.apiName,
        modifiedDesc: trait.modifiedDesc || trait.desc,
      };
      traitCache.set(trait.apiName, tftTrait);
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
