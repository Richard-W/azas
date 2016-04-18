import {Component, OnInit, NgZone} from 'angular2/core';
import {TokenComponent} from './token.component';
import {CouncilComponent} from './council.component';
import {AzasService} from './azas.service';

@Component({
    selector: 'azas',
    directives: [
        TokenComponent,
        CouncilComponent
    ],
    template:`
        <h1>{{title}}</h1>
        <pre *ngIf="error != null">{{error}}</pre>
        <azas-token *ngIf="displayComponent == 'Token' && error == null" (onToken)="onToken($event)"></azas-token>
        <azas-council *ngIf="displayComponent == 'Council' && error == null" [token]="token"></azas-council>
    `
})
export class AppComponent implements OnInit {
    private displayComponent = 'Token';
    private token = '';
    private title = '';
    private error: string = null;

    constructor(private azas: AzasService, private zone: NgZone) {}

    public ngOnInit() {
        this.azas.getMetaInfo().subscribe(
            success => {
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
        this.displayComponent = 'Council';
    }
}