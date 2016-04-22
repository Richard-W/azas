import {Injectable} from 'angular2/core';
import {Http} from 'angular2/http';
import 'rxjs/Rx';
import {Observable} from 'rxjs/Rx';
import {Participant, Mascot, Council, MetaInfo, DataDump} from './types';

@Injectable()
export class AzasService {
	public static host: string;

	constructor(private http: Http) {}

	private call(endpoint: string, request: Object): Observable<Object> {
		var body = JSON.stringify(request);
		return this.http.post(AzasService.host + endpoint, body).
			map(response => response.json())
	}

	public getCouncil(token: string): Observable<Council> {
		return this.call('/v1/getcouncil', {'token': token}).
			map(obj => (<Council> obj));
	}

	public addParticipant(token: string, info: any, priority: number): Observable<string> {
		return this.call('/v1/addpart', {'token': token, 'info': info, 'priority': priority}).
			map(obj => (<string> (<any> obj).id));
	}

	public deleteParticipant(token: string, id: string): Observable<void> {
		return this.call('/v1/delpart', {'token': token, 'id': id}).
			map(obj => { return; });
	}

	public editParticipant(token: string, id: string, info: any, priority: number): Observable<void> {
		return this.call('/v1/editpart', {'token': token, 'id': id, 'info': info, 'priority': priority}).
			map(obj => { return; });
	}

	public addMascot(token: string, fullName: string, nickName: string): Observable<string> {
		return this.call('/v1/addmascot', {'token': token, 'fullName': fullName, 'nickName': nickName}).
			map(obj => (<string> (<any> obj).id));
	}

	public editMascot(token: string, id: string, fullName: string, nickName: string): Observable<void> {
		return this.call('/v1/editmascot', {'token': token, 'id': id, 'fullName': fullName, 'nickName': nickName}).
			map(obj => { return; });
	}

	public deleteMascot(token: string, id: string): Observable<void> {
		return this.call('/v1/delmascot', {'token': token, 'id': id}).
			map(obj => { return; });
	}

	public getMetaInfo(): Observable<MetaInfo> {
		return this.call('/v1/metainfo', {}).
			map(obj => (<MetaInfo> obj));
	}

	public dumpData(password: string): Observable<DataDump> {
		return this.call('/v1/dumpdata', {'password': password}).
			map(obj => (<DataDump> obj));
	}
}
