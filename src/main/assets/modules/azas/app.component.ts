import {Component, OnInit, NgZone} from 'angular2/core';
import {TokenComponent} from './token.component';
import {CouncilComponent} from './council.component';
import {PasswordComponent} from './password.component';
import {AdminComponent} from './admin.component';
import {AzasService} from './azas.service';
import {MetaInfo} from './types';

@Component({
	selector: 'azas',
	directives: [
		TokenComponent,
		CouncilComponent,
		PasswordComponent,
		AdminComponent
	],
	template:`
	<div id="azas">
		<h1>{{title}}</h1>
		<div             *ngIf="error != null"                                                                                     ><pre>{{error}}</pre></div>
		<azas-token      *ngIf="displayComponent == 'Token'   && error == null"                 (onToken)="onToken($event)"        ></azas-token>
		<azas-council    *ngIf="displayComponent == 'Council' && error == null && meta != null" [token]="token" [meta]="meta"      ></azas-council>
		<azas-masterpass *ngIf="displayComponent == 'Pass'    && error == null"                 (password)="onPassword($event)"    ></azas-masterpass>
		<azas-admin      *ngIf="displayComponent == 'Admin'   && error == null && meta != null" [password]="password" [meta]="meta"></azas-admin>
	</div>
	`
})
export class AppComponent implements OnInit {
	private displayComponent = 'Token';
	private token = '';
	private password = '';
	private title = '';
	private error: string = null;
	private meta: MetaInfo = null;

	constructor(private azas: AzasService, private zone: NgZone) {}

	public ngOnInit() {
		if (window.location.hash.substring(1) == "azasadmin") {
			this.displayComponent = "Pass";
		}
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

	private onPassword(event: string) {
		this.password = event;
		setTimeout(() => { this.displayComponent = 'Admin'; }, 0);
	}
}
