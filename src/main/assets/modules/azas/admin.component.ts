import {Component, Input, OnInit, NgZone} from 'angular2/core';
import {AzasService} from './azas.service';
import {MetaInfo, DataDump} from './types';

@Component({
	selector: 'azas-admin',
	template:`
		<h2>Administration</h2>
		<div *ngIf="error != ''"><pre>{{error}}</pre></div>
		<div *ngIf="error == '' && data != null"><pre>{{dataString}}</pre></div>
	`
})
export class AdminComponent implements OnInit {
	@Input() password: string;
	@Input() meta: MetaInfo;

	private error: string = '';
	private data: DataDump = null;
	private dataString: string = '';

	constructor(private azas: AzasService, private zone: NgZone) {}

	public ngOnInit() {
		this.reloadData();
	}

	private reloadData() {
		this.azas.dumpData(this.password).subscribe(
			success => {
				this.data = success;
				this.dataString = JSON.stringify(this.data, null, 2);
				this.zone.run(() => {});
			}, error => {
				this.error = JSON.stringify(error, null, 2);
				this.zone.run(() => {});
			}
		);
	}
}
