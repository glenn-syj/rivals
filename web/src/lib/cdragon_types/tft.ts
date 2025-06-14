export interface TftChampion {
  apiName: string;
  name: string;
  ability?: {
    modifiedDesc: string;
  };
}

export interface TftItem {
  apiName: string;
  name: string;
  modifiedDesc: string;
}

export interface TftTrait {
  apiName: string;
  name: string;
  modifiedDesc: string;
}

export interface TftSetData {
  champions: TftChampion[];
  traits: TftTrait[];
}

export interface TftData {
  sets: {
    [key: string]: TftSetData;
  };
  items: TftItem[];
}
