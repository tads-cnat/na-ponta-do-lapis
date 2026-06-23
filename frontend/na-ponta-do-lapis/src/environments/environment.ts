export const environment = {
    production: true,
    apiBaseUrl: (() => {
        const protocol = window.location.protocol;
        const hostname = window.location.hostname;

        if (hostname.includes('.github.dev')) {
            return `${protocol}//${hostname.replace('4200', '8080')}`;
        }

        return `${protocol}//${hostname}:8080`;
    })(),
};