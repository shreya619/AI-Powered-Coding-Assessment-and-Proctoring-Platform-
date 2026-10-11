import { Injectable } from '@angular/core';
import * as faceapi from 'face-api.js';

@Injectable({
  providedIn: 'root'
})
export class FaceDetectionService {
  private modelsLoaded = false;

  async loadModels(): Promise<void> {
    if (this.modelsLoaded) return;

    try {
      await faceapi.nets.tinyFaceDetector.loadFromUri('/assets/models/tiny_face_detector');
      await faceapi.nets.faceLandmark68Net.loadFromUri('/assets/models/face_landmark_68');
      await faceapi.nets.faceRecognitionNet.loadFromUri('/assets/models/face_recognition');
      this.modelsLoaded = true;
      console.log('Face detection models loaded successfully');
    } catch (error) {
      console.error('Failed to load face detection models:', error);
      throw error;
    }
  }

  async detectFaces(videoElement: HTMLVideoElement, canvas?: HTMLCanvasElement): Promise<any[]> {
    if (!this.modelsLoaded) {
      await this.loadModels();
    }

    // Check if video is ready
    if (!videoElement || videoElement.readyState < 2) {
      console.log('Video not ready for face detection');
      return [];
    }

    try {
      const detections = await faceapi
        .detectAllFaces(videoElement, new faceapi.TinyFaceDetectorOptions({ inputSize: 224, scoreThreshold: 0.5 }))
        .withFaceLandmarks();
      
      console.log(`Detected ${detections.length} face(s)`);
      
      // Draw detections on canvas if provided
      if (canvas && detections.length > 0) {
        const displaySize = { width: videoElement.width, height: videoElement.height };
        faceapi.matchDimensions(canvas, displaySize);
        const resized = faceapi.resizeResults(detections, displaySize);
        const ctx = canvas.getContext('2d');
        if (ctx) {
          ctx.clearRect(0, 0, canvas.width, canvas.height);
          faceapi.draw.drawDetections(canvas, resized);
          faceapi.draw.drawFaceLandmarks(canvas, resized);
        }
      }
      
      return detections;
    } catch (error) {
      console.error('Face detection error:', error);
      return [];
    }
  }

  analyzeFaceData(detections: any[]): {
    faceCount: number;
    faceVisible: boolean;
    lookingAway: boolean;
    multipleFaces: boolean;
  } {
    const faceCount = detections.length;
    
    return {
      faceCount,
      faceVisible: faceCount > 0,
      lookingAway: faceCount === 0,
      multipleFaces: faceCount > 1
    };
  }
}