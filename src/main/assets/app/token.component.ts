import {Component, Output, EventEmitter} from 'angular2/core';

@Component({
    selector: 'azas-token',
    template:`
        <label for="token">Token:</label><br />
        <input type="text" id="token" [(ngModel)]="token" />
        <button (click)="onTokenSubmit()">Abschicken</button>
    `
})
export class TokenComponent {
    private token: string = '';

    @Output() onToken: EventEmitter<string> = new EventEmitter<string>();

    private onTokenSubmit() {
        this.onToken.emit(this.token);
    }
}
