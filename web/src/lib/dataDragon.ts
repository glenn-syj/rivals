import { TftSet, TftChampion, TftItem, TftTrait } from "./dataDragonTypes";

const DD_VERSION = process.env.NEXT_PUBLIC_DD_VERSION;
const LANG_SET = process.env.NEXT_PUBLIC_LANG_SET;
const BASE_CDN = process.env.NEXT_PUBLIC_DD_BASE_CDN;
const GAME_DATA_URL = `${BASE_CDN}/${DD_VERSION}/data/${LANG_SET}`;
const CHAMPION_DATA_URL = `${BASE_CDN}/${DD_VERSION}/data/${LANG_SET}/tft-champion.json`;
const ITEM_DATA_URL = `${BASE_CDN}/${DD_VERSION}/data/${LANG_SET}/tft-item.json`;
const TRAIT_DATA_URL = `${BASE_CDN}/${DD_VERSION}/data/${LANG_SET}/tft-trait.json`;
const GAME_IMAGE_URL = `${BASE_CDN}/${DD_VERSION}/img`;

export class DataDragonService {
  private static instance: DataDragonService;
  private championData: Record<string, TftChampion> | null = null;
  private itemData: Record<string, TftItem> | null = null;
  private traitData: Record<string, TftTrait> | null = null;

  private constructor() {}

  static getInstance(): DataDragonService {
    if (!DataDragonService.instance) {
      DataDragonService.instance = new DataDragonService();
    }
    return DataDragonService.instance;
  }

  async initialize() {
    if (!this.championData) {
      await this.fetchChampionData();
    }
    if (!this.itemData) {
      await this.fetchItemData();
    }
    if (!this.traitData) {
      await this.fetchTraitData();
    }
  }

  private async fetchChampionData() {
    const response = await fetch(CHAMPION_DATA_URL);
    const data = await response.json();
    this.championData = data.data;
  }

  private async fetchItemData() {
    const response = await fetch(ITEM_DATA_URL);
    const data = await response.json();
    this.itemData = data.data;
  }

  private async fetchTraitData() {
    const response = await fetch(TRAIT_DATA_URL);
    const data = await response.json();
    this.traitData = data.data;
  }

  private findChampionByShortId(shortId: string): TftChampion | undefined {
    if (!this.championData) return undefined;

    return Object.values(this.championData).find(
      (champion) => champion.id === shortId
    );
  }

  private findItemByShortId(shortId: string): TftItem | undefined {
    if (!this.itemData) return undefined;

    return Object.values(this.itemData).find((item) => item.id === shortId);
  }

  private findTraitByShortId(shortId: string): TftTrait | undefined {
    if (!this.traitData) return undefined;

    return Object.values(this.traitData).find((trait) => trait.id === shortId);
  }

  getChampionName(shortId: string): string {
    const champion = this.findChampionByShortId(shortId);
    return champion?.name || "";
  }

  getChampionImage(shortId: string): string {
    const champion = this.findChampionByShortId(shortId);
    if (champion) {
      return `${GAME_IMAGE_URL}/tft-champion/${champion.image.full}`;
    }
    return "";
  }

  getItemName(shortId: string): string {
    const item = this.findItemByShortId(shortId);
    return item?.name || "";
  }

  getItemImage(shortId: string): string | null {
    const item = this.findItemByShortId(shortId);
    if (item) {
      return `${GAME_IMAGE_URL}/tft-item/${item.image.full}`;
    }
    return null;
  }

  getTraitName(shortId: string): string {
    const trait = this.findTraitByShortId(shortId);
    return trait?.name || "";
  }

  getTraitImage(shortId: string): string {
    const trait = this.findTraitByShortId(shortId);
    if (trait) {
      return `${GAME_IMAGE_URL}/tft-trait/${trait.image.full}`;
    }
    return "";
  }
}

export const dataDragonService = DataDragonService.getInstance();
