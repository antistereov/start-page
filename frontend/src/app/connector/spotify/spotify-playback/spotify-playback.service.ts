import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SpotifyPlaybackService {
    private player: Spotify.Player | null = null;
    private deviceId: string | null = null;

    constructor() {
        this.loadSpotifySDK();
    }

    async togglePlay() {
        console.log("Toggle play")
        await this.player!.togglePlay()
    }

    private loadSpotifySDK(): void {
        const script = document.createElement('script');
        script.src = 'https://sdk.scdn.co/spotify-player.js';
        script.type = 'text/javascript';
        script.onload = () => {
            this.initializePlayer().then();
        }
        document.head.appendChild(script);
    }

    private async initializePlayer(): Promise<void> {
        window.onSpotifyWebPlaybackSDKReady = () => {
            const token = 'BQDaYN_-zon8hzOg365UDRGbXspWEgF7cx2qr3XvSSXItfUyI2TzPzA3VH2Vs9QdzBoyD9AMePJ5EwD67kAdaNduwG5op2-vh0ziJKZoIgg0YDDqbegxbECu8nK5P2cHIKMp2en3eqwFOClA6nA3Di3Dlkvq5g44Qv1HKQBKU4ak0Hk9EIWZHbXW8ZB_u4vlIXaCp-bVbj18B-DJ2Fod5VRRgsrq0aQXEuyU0t1CoeBLjdD-C3oyLJFZltSkzu-FOlan5lN0CTqbSPJAVhkqBF4_34Ki6CDt1W87i5d0mtk_1SMIiMM2fs0YicDBKGofR1pZ1LMwUppR7NdO1MywnVzRBt6y75o';
            this.player = new Spotify.Player({
                name: 'Spotify Player',
                getOAuthToken: (cb) => { cb(token); },
                volume: 0.5
            });

            console.log("Spotify SDK loaded");

            this.player!.connect().then(success => {
                if (success) {
                    console.log("The player successfully connected");
                } else {
                    console.error("The player failed to connect");
                }
            }).catch(error => {
                console.error("Failed to connect to the Spotify Player", error);
            });

            // Add event listeners
            this.player!.addListener('ready', ({ device_id }: { device_id: string }) => {
                console.log('Ready with Device ID', device_id);
                this.deviceId = device_id;
            });

            this.player!.addListener('not_ready', ({ device_id }: { device_id: string }) => {
                console.log('Device ID has gone offline', device_id);
            });

            this.player!.addListener('initialization_error', ({ message }: { message: string }) => {
                console.error('Initialization Error:', message);
            });

            this.player!.addListener('authentication_error', ({ message }: { message: string }) => {
                console.error('Authentication Error:', message);
            });

            this.player!.addListener('account_error', ({ message }: { message: string }) => {
                console.error('Account Error:', message);
            });

            this.player!.addListener('playback_error', ({ message }: { message: string }) => {
                console.error('Playback Error:', message);
            });

            this.player!.addListener('player_state_changed', ({
                    position,
                    duration,
                    track_window: { current_track }
                }) => {
                console.log('Currently Playing', current_track);
                console.log('Position in Song', position);
                console.log('Duration of Song', duration);
            });
        }
    }
}
