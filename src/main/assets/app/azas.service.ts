import {Injectable} from 'angular2/core';
import {Http} from 'angular2/http';
import 'rxjs/Rx';
import {Observable} from 'rxjs/Rx';

@Injectable()
export class AzasService {
    public static host: string;

    constructor(private http: Http) {}

    private call(endpoint: string, request: Object): Observable<Object> {
        var body = JSON.stringify(request);
        return this.http.post(AzasService.host + endpoint, body)
            .map(response => response.json())
    }

    public getCouncil(token: string): Observable<Object> {
        return this.call('/v1/getcouncil', {'token': token});
    }

    public addParticipant(token: string, info: any, priority: number) {
        return this.call('/v1/addpart', {'token': token, 'info': info, 'priority': priority});
    }
}
