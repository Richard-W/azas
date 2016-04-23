import {Component, Input, Output, EventEmitter} from 'angular2/core';
import {Mascot} from './types';

@Component({
	selector: 'azas-displaymascots',
	template:`
	<p *ngIf="mascots.length == 0">Noch keine Maskottchen</p>
	<table *ngIf="mascots.length > 0">
		<thead>
			<tr>
				<th>Voller Name</th>
				<th>Spitzname</th>
			</tr>
		</thead>
		<tbody>
			<tr *ngFor="#mascot of mascots">
				<td>{{mascot.fullName}}</td>
				<td>{{mascot.nickName}}</td>
				<td *ngFor="#action of actions" class="azas-buttoncell"><button (click)="onAction(action.id, mascot)">{{action.name}}</button></td>
			</tr>
	<table>
	`
})
export class DisplayMascotsComponent {
	@Input() mascots: Mascot[];
	@Input() actions: [{id: number, name: string}];

	@Output() action: EventEmitter<{ id: number, target: Mascot }> = new EventEmitter<{ id: number, target: Mascot }>();

	private onAction(id: number, target: Mascot) {
		this.action.emit({id, target});
	}
}
