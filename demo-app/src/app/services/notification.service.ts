import { inject, Injectable } from '@angular/core';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly snackBar = inject(MatSnackBar);

  private show(message: string, type: 'error' | 'success' | 'info', duration = 6000): void {
    const config: MatSnackBarConfig = {
      duration,
      horizontalPosition: 'end',
      verticalPosition: 'bottom',
      panelClass: [`snack-${type}`],
    };
    this.snackBar.open(message, '✕', config);
  }

  success(message: string): void {
    this.show(message, 'success', 4000);
  }

  info(message: string): void {
    this.show(message, 'info', 4000);
  }

  error(message: string): void {
    this.show(message, 'error', 7000);
  }

  /**
   * Parses an HttpErrorResponse and returns a human-readable message.
   * Handles validation maps, single-message objects, constraint violations, and generic errors.
   */
  parseAndShowError(err: HttpErrorResponse, fallback = 'An unexpected error occurred.'): void {
    const msg = this.extractMessage(err, fallback);
    this.error(msg);
  }

  extractMessage(err: HttpErrorResponse, fallback = 'An unexpected error occurred.'): string {
    if (!err.error) {
      if (err.status === 0) return 'Cannot connect to the server. Please make sure the backend is running.';
      if (err.status === 404) return 'The requested resource was not found.';
      if (err.status === 409) return 'A duplicate entry or constraint violation occurred.';
      if (err.status === 500) return 'An internal server error occurred. Please try again.';
      return fallback;
    }

    const body = err.error;

    // Constraint violation (409 from DataIntegrityViolationException)
    if (err.status === 409) {
      if (body.error) return body.error;
      return 'This action cannot be completed because it would violate a database constraint. ' +
             'The record may be referenced by other data (e.g., a genre still assigned to books).';
    }

    // Our custom ValidationException → { error: "..." }
    if (typeof body.error === 'string') {
      return body.error;
    }

    // Our custom ValidationException → { message: "..." }
    if (typeof body.message === 'string') {
      return body.message;
    }

    // Bean Validation → { field: "message", field2: "message2" }
    if (typeof body === 'object' && !Array.isArray(body)) {
      const entries = Object.entries(body) as [string, string][];
      if (entries.length > 0 && entries.every(([, v]) => typeof v === 'string')) {
        return entries.map(([field, msg]) => `${capitalizeField(field)}: ${msg}`).join('\n');
      }
    }

    return fallback;
  }
}

function capitalizeField(field: string): string {
  return field.replace(/([A-Z])/g, ' $1').replace(/^./, s => s.toUpperCase());
}