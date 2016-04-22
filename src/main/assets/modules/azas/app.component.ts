import {Component, OnInit, NgZone} from 'angular2/core';
import {TokenComponent} from './token.component';
import {CouncilComponent} from './council.component';
import {AzasService} from './azas.service';
import {MetaInfo} from './types';

@Component({
	selector: 'azas',
	directives: [
		TokenComponent,
		CouncilComponent
	],
	template:`
	<div id="azas">
		<h1>{{title}}</h1>
		<div          *ngIf="error != null"                                                                               ><pre>{{error}}</pre></div>
		<azas-token   *ngIf="displayComponent == 'Token'   && error == null"                 (onToken)="onToken($event)"  ></azas-token>
		<azas-council *ngIf="displayComponent == 'Council' && error == null && meta != null" [token]="token" [meta]="meta"></azas-council>
	</div>
	`
})
export class AppComponent implements OnInit {
	private displayComponent = 'Token';
	private token = '';
	private title = '';
	private error: string = null;
	private meta: MetaInfo = null;

	constructor(private azas: AzasService, private zone: NgZone) {}

	public ngOnInit() {
		this.azas.getMetaInfo().subscribe(
			success => {
				this.meta = success;
				this.title = (<any> success).title;
				this.zone.run(() => {});
			}, error => {
				this.error = JSON.stringify(error, null, 2);
				this.zone.run(() => {});
			}
		);
	}

	private onToken(event: string) {
		this.token = event;
		setTimeout(() => { this.displayComponent = 'Council'; }, 0);
	}
}
