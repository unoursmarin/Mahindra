import type { Map as MaplibreMap } from 'maplibre-gl';
import type { FeatureCollection } from 'geojson';
import type { Country } from '../../../models/country.model';

const SOURCE_ID = 'countries';
const LABELS_LAYER_ID = 'country-labels';

const BOUNDARY_SOURCE_ID = 'country-boundaries';
const BOUNDARY_FILL_LAYER_ID = 'country-boundary-fill';
const BOUNDARY_LINE_LAYER_ID = 'country-boundary-line';
const DETECTION_LAYER_ID = 'country-detection';

// Simplified world boundaries GeoJSON (low resolution, ~2MB)
const BOUNDARIES_URL =
  'https://raw.githubusercontent.com/datasets/geo-countries/master/data/countries.geojson';

export function addCountrySource(map: MaplibreMap, geojson: FeatureCollection): void {
  map.addSource(SOURCE_ID, { type: 'geojson', data: geojson });
}

export function addCountryLayers(map: MaplibreMap): void {
  map.addLayer({
    id: LABELS_LAYER_ID,
    type: 'symbol',
    source: SOURCE_ID,
    minzoom: 1,
    layout: {
      'text-field': ['get', 'name'],
      'text-size': ['interpolate', ['linear'], ['zoom'], 1, 10, 5, 13],
      'text-anchor': 'center',
      'text-allow-overlap': false,
    },
    paint: {
      'text-color': '#e2e8f0',
      'text-halo-color': '#0a0e17',
      'text-halo-width': 1.5,
    },
  });
}

let boundarySourceLoaded = false;

export function ensureBoundarySource(map: MaplibreMap): void {
  if (boundarySourceLoaded || map.getSource(BOUNDARY_SOURCE_ID)) {
    boundarySourceLoaded = true;
    return;
  }

  map.addSource(BOUNDARY_SOURCE_ID, {
    type: 'geojson',
    data: BOUNDARIES_URL,
  });

  // Fill layer — hidden until a country is selected
  map.addLayer(
    {
      id: BOUNDARY_FILL_LAYER_ID,
      type: 'fill',
      source: BOUNDARY_SOURCE_ID,
      filter: ['==', ['get', 'ISO_A2'], ''],
      paint: {
        'fill-color': '#f59e0b',
        'fill-opacity': 0.18,
      },
    },
    LABELS_LAYER_ID, // insert below the country name labels
  );

  // Outline layer
  map.addLayer(
    {
      id: BOUNDARY_LINE_LAYER_ID,
      type: 'line',
      source: BOUNDARY_SOURCE_ID,
      filter: ['==', ['get', 'ISO_A2'], ''],
      paint: {
        'line-color': '#f59e0b',
        'line-width': 1.5,
        'line-opacity': 0.8,
      },
    },
    LABELS_LAYER_ID,
  );

  // Invisible detection layer covering all countries — used by queryRenderedFeatures
  // in the zoom handler to identify which country polygon is under the map center.
  map.addLayer(
    {
      id: DETECTION_LAYER_ID,
      type: 'fill',
      source: BOUNDARY_SOURCE_ID,
      paint: {
        'fill-color': '#000000',
        'fill-opacity': 0,
      },
    },
    BOUNDARY_FILL_LAYER_ID, // insert below the visible highlight so ordering is stable
  );

  boundarySourceLoaded = true;
}

export function highlightCountry(map: MaplibreMap, country: Country | null): void {
  // Polygon boundary highlight (from Natural Earth dataset)
  if (map.getLayer(BOUNDARY_FILL_LAYER_ID)) {
    const iso2 = country?.cca2 ?? '';
    map.setFilter(BOUNDARY_FILL_LAYER_ID, ['==', ['get', 'ISO_A2'], iso2]);
    map.setFilter(BOUNDARY_LINE_LAYER_ID, ['==', ['get', 'ISO_A2'], iso2]);
  }
}

export function countryFromFeature(
  properties: Record<string, unknown>,
  coordinates: [number, number],
): Country {
  return {
    id: properties['id'] as number,
    name: properties['name'] as string,
    capital: (properties['capital'] as string) ?? null,
    population: (properties['population'] as number) ?? null,
    region: (properties['region'] as string) ?? null,
    area: (properties['area'] as number) ?? null,
    flagUrl: (properties['flagUrl'] as string) ?? null,
    cca2: (properties['cca2'] as string) ?? null,
    capitalLng: coordinates[0],
    capitalLat: coordinates[1],
    emoji: (properties['emoji'] as string) ?? null,
    phoneCode: (properties['phoneCode'] as string) ?? null,
  };
}

/** Builds an ISO2 → Country lookup map from the countries GeoJSON. */
export function buildCountryLookup(geojson: FeatureCollection): Map<string, Country> {
  const lookup = new Map<string, Country>();
  for (const feature of geojson.features) {
    if (feature.geometry?.type !== 'Point') continue;
    const coords = (feature.geometry as GeoJSON.Point).coordinates as [number, number];
    const country = countryFromFeature(feature.properties as Record<string, unknown>, coords);
    if (country.cca2) {
      lookup.set(country.cca2.toUpperCase(), country);
    }
  }
  return lookup;
}

export { LABELS_LAYER_ID, DETECTION_LAYER_ID };
