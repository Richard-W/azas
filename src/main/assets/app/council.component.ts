import {Component, Input, OnInit} from 'angular2/core';
import {AzasService} from './azas.service';

@Component({
    selector: 'azas-council',
    template:`
        <div *ngIf="council == null && error == ''">
            <p>Loading...</p>
        </div>
        <div *ngIf="error != ''">
            <p><b>Error</b></p>
            <p>{{error}}</p>
        </div>
        <div *ngIf="council != null">
            <h2>{{council.info.university}}</h2>
            <h3>Teilnehmer</h3>
            <p>{{councilString}}</p>
            <table>
                <tbody>
                    <tr *ngFor="#participant of council.participants">
                        <td>{{participant.info.name}}</td>
                        <td>{{participant.info.email}}</td>
                    </tr>
                </tbody>
            </table>
            <h3>Maskottchen</h3>
            <h3>Debug</h3>
            <button (click)="onDebugClick()">Test</button>
        <div>
    `
})
export class CouncilComponent implements OnInit {
    @Input() token: string;

    private council: any = null;
    private councilString: string = '';
    private error: string = '';

    constructor(private azas: AzasService) {}

    public ngOnInit() {
        this.reloadCouncil();
    }

    private reloadCouncil() {
        this.azas.getCouncil(this.token).subscribe(
            council => {
                this.council = council;
                this.councilString = JSON.stringify(council);
            },
            error => {
                this.error = JSON.stringify(error);
            }
        );
    }

    private onDebugClick() {
        this.azas.addParticipant(this.token, {
            'name': 'Testity test',
            'email': 'testity@example.org',
            'address': {
                'street': 'a',
                'zipCode': '0',
                'city': 'b',
                'country': 'c'
            }
        }, 5).subscribe(
            response => {
                this.reloadCouncil();
            },
            error => {
                this.error = JSON.stringify(error);
            }
        );
    }
}