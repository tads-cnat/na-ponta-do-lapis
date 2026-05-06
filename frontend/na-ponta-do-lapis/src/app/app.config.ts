import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import Aura from '@primeuix/themes/aura';
import { providePrimeNG } from 'primeng/config';
import { definePreset } from '@primeuix/themes';

const naPontaDoLapisTema = definePreset(Aura, {
    semantic: {
        primary: {
            50: '{purple.50}',
            100: '{purple.100}',
            200: '{purple.200}',
            300: '{purple.300}',
            400: '{purple.400}',
            500: '{purple.500}',
            600: '{purple.600}',
            700: '{purple.700}',
            800: '{purple.800}',
            900: '{purple.900}',
            950: '{purple.950}'
        }
    }
});

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    providePrimeNG({
      theme : {
        preset: naPontaDoLapisTema,
        options: {
          darkModeSelector: false || 'none',
          CssLayer: {
            name: 'primeng',
            order: 'theme, base, primeng' 
          }
        }
      }
    })
  ]
};
