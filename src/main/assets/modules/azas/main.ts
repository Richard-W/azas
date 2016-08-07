import {bootstrap}      from 'angular2/platform/browser';
import {enableProdMode} from 'angular2/core';
import {AppComponent}   from './app.component';
import {CouncilComponent} from './council.component';
import {Council}        from './types'
import {AzasService}    from './azas.service';
import {HTTP_PROVIDERS} from 'angular2/http';

export function azasBootstrap(host: string) {
	enableProdMode();
	AzasService.host = host;
	bootstrap(AppComponent, [HTTP_PROVIDERS, AzasService]);
}

export function azasConsumeCouncil(f: (Council) => void) {
	CouncilComponent.consumeCouncil = f;
}
