import {Component, Input, OnInit, NgZone} from 'angular2/core';
import {AzasService} from './azas.service';
import {ParticipantFormComponent} from './participantform.component'

@Component({
    selector: 'azas-council',
    directives: [ParticipantFormComponent],
    template:`
        <div *ngIf="council == null && error == ''">
            <p>Loading...</p>
        </div>
        <div *ngIf="error != ''">
            <pre>{{error}}</pre>
        </div>
        <div *ngIf="council != null">
            <h2>{{council.info.university}}</h2>
            <h3>Teilnehmer</h3>
            <table>
                <tbody>
                    <tr *ngFor="#participant of council.participants">
                        <td>{{participant.info.name}}</td>
                        <td>{{participant.info.email}}</td>
                        <td><button (click)="deleteParticipant(participant.id)">Delete</button></td>
                    </tr>
                </tbody>
            </table>
            <participant-form *ngIf="meta != null" [meta]="meta" (submitForm)="onSubmit($event)"></participant-form>
            <h3>Maskottchen</h3>
        <div>
    `
})
export class CouncilComponent implements OnInit {
    @Input() token: string;
    @Input() meta: any;

    private council: any = null;
    private error: string = '';
    private types: string[] = null
    private form: any = {};

    constructor(private azas: AzasService, private zone: NgZone) {}

    public ngOnInit() {
        this.reloadCouncil();
    }

    private keys(obj: Object): string[] {
        return Object.keys(obj);
    }

    private typeFields(field: string): any[] {
        return this.meta.types[field];
    }

    private reloadCouncil() {
        this.azas.getCouncil(this.token).subscribe(
            council => {
                this.council = council;
                this.zone.run(() => {});
            },
            error => {
                this.error = JSON.stringify(error, null, 2);
                this.zone.run(() => {});
            }
        );
    }

    private deleteParticipant(id: string) {
        this.azas.deleteParticipant(this.token, id).subscribe(
            success => {
                this.reloadCouncil();
            },
            error => {
                this.error = JSON.stringify(error, null, 2);
            }
        );
    }

    private onSubmit(event: any) {
        this.azas.addParticipant(this.token, event, 0).subscribe(
            success => {
                this.reloadCouncil();
            }, error => {
                this.error = JSON.stringify(error, null, 2);
            }
        );
    }
}