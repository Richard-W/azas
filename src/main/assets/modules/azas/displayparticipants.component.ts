import {Component, Input, Output, EventEmitter} from 'angular2/core';
import {Participant, MetaInfo} from './types';

@Component({
	selector: 'azas-displayparticipants',
	template:`
	<p *ngIf="participants.length == 0">Noch keine Anmeldungen</p>
	<table *ngIf="participants.length > 0" id="ptable">
		<thead>
			<tr>
				<th></th>
				<th *ngFor="#name of participantFieldNames()">{{name}}</th>
			</tr>
		</thead>
		<tbody>
			<tr *ngFor="#participant of participants" [attr.id]="'p'+participant.id" [ngClass]="{approved: participant.approved, notApproved: !participant.approved}">
				<td *ngIf="participant.approved" class="approval">&#10003;</td>
				<td *ngIf="!participant.approved" class="approval">&nbsp;</td>
				<td *ngFor="#field of participantFields(participant)">{{field}}</td>
				<td *ngFor="#action of actions"><button (click)="onAction(action.id, participant)">{{action.name}}</button></td>
			</tr>
		</tbody>
	</table>
	`
})
export class DisplayParticipantsComponent {
	@Input() participants: Participant[];
	@Input() actions: [{id: number, name: string}];
	@Input() meta: MetaInfo;
	
	@Output() action: EventEmitter<{ id: number, target: Participant }> = new EventEmitter<{ id: number, target: Participant }>();

	private participantFieldNames() {
		var fields: string[] = [];
		for (var i = 0; i < this.meta.numDisplayedParticipantFields; ++i) {
			var field = this.meta.types[this.meta.participantType][i];
			fields.push(field.name);
		}
		return fields;
	}

	private participantFields(participant: any) {
		var fields: string[] = [];
		for (var i = 0; i < this.meta.numDisplayedParticipantFields; ++i) {
			var field = this.meta.types[this.meta.participantType][i];
			fields.push(participant.info[field.field]);
		}
		return fields;
	}

	private onAction(id: number, target: Participant) {
		this.action.emit({id, target});
	}
}
