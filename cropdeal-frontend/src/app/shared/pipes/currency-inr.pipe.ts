import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'inr',
  standalone: true
})
export class CurrencyInrPipe implements PipeTransform {
  transform(value: number | null | undefined, showSymbol: boolean = true): string {
    if (value === null || value === undefined || isNaN(value)) {
      return showSymbol ? '₹0.00' : '0.00';
    }
    const formatted = new Intl.NumberFormat('en-IN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(value);

    return showSymbol ? `₹${formatted}` : formatted;
  }
}
