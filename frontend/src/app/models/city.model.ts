export interface City {
  id: number;
  name: string;
  countryId: number;
  population: number | null;
  zipCode: string | null;
  description: string | null;
  latitude: number | null;
  longitude: number | null;
  stateCode: string | null;
  stateName: string | null;
}
