import {Component} from 'angular2/core';
import {TokenComponent} from './token.component';
import {CouncilComponent} from './council.component';

@Component({
    selector: 'azas',
    directives: [
        TokenComponent,
        CouncilComponent
    ],
    template:`
        <h1>Anmeldung zur ZaPF</h1>
        <azas-token *ngIf="displayComponent == 'Token'" (onToken)="onToken($event)"></azas-token>
        <azas-council *ngIf="displayComponent == 'Council'" [token]="token"></azas-council>
    `
})
export class AppComponent {
    private displayComponent = 'Token';
    private token = '';

    private onToken(event: string) {
        this.token = event;
        this.displayComponent = 'Council';
    }
}