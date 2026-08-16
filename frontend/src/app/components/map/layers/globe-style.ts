import type { StyleSpecification } from 'maplibre-gl';

export function buildGlobeStyle(): StyleSpecification {
  return {
    version: 8,
    glyphs: 'https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf',
    sources: {
      demotiles: {
        type: 'vector',
        tiles: ['https://demotiles.maplibre.org/tiles/{z}/{x}/{y}.pbf'],
        maxzoom: 6,
      },
    },
    layers: [
      {
        id: 'background',
        type: 'background',
        paint: { 'background-color': '#0a0e17' },
      },
      {
        id: 'land-fill',
        type: 'fill',
        source: 'demotiles',
        'source-layer': 'countries',
        paint: {
          'fill-color': '#111827',
          'fill-opacity': 0.9,
        },
      },
      {
        id: 'land-borders',
        type: 'line',
        source: 'demotiles',
        'source-layer': 'countries',
        paint: {
          'line-color': '#1e3a5f',
          'line-width': 0.8,
          'line-opacity': 0.7,
        },
      },
    ],
  };
}
