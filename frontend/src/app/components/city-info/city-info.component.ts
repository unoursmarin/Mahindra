import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { City } from '../../models/city.model';

@Component({
  selector: 'app-city-info',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './city-info.component.html',
  styleUrl: './city-info.component.scss',
})
export class CityInfoComponent {
  city = input<City | null>(null);
  countryName = input<string | null>(null);
  close = output<void>();

  onClose(): void {
    this.close.emit();
  }

  formatCoord(value: number | null): string {
    if (value == null) return '—';
    return value.toFixed(4);
  }
}
