declare module "badwords-ko" {
  interface FilterOptions {
    emptyList?: boolean;
    list?: string[];
    exclude?: string[];
    placeHolder?: string;
    regex?: RegExp;
    replaceRegex?: RegExp;
    splitRegex?: RegExp;
  }

  class Filter {
    constructor(options?: FilterOptions);
    isProfane(string: string): boolean;
    clean(string: string): string;
    addWords(...words: string[]): void;
    removeWords(...words: string[]): void;
  }

  export default Filter;
}
