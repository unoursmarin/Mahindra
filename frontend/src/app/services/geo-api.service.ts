import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import type { FeatureCollection, Feature } from 'geojson';
import { environment } from '../../environments/environment';
import { Country } from '../models/country.model';
import { PaginatedResponse } from '../models/paginated-response.model';
import { City } from '../models/city.model';

@Injectable({ providedIn: 'root' })
export class GeoApiService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getCountries(): Observable<Country[]> {
    return this.http.get<Country[]>(`${this.baseUrl}/countries`);
  }

  getCities(countryId: number, page = 0, size = 100): Observable<PaginatedResponse<City>> {
    return this.http.get<PaginatedResponse<City>>(
      `${this.baseUrl}/countries/${countryId}/cities?page=${page}&size=${size}`,
    );
  }

  getCountriesGeoJson(): Observable<FeatureCollection> {
    return this.http.get<FeatureCollection>(`${this.baseUrl}/countries/geojson`);
  }

  getCountryGeoJson(id: number): Observable<Feature> {
    return this.http.get<Feature>(`${this.baseUrl}/countries/${id}/geojson`);
  }

  getCitiesGeoJson(id: number): Observable<FeatureCollection> {
    return this.http.get<FeatureCollection>(`${this.baseUrl}/countries/${id}/cities/geojson`);
  }
}
