import { parseVoiceCommand } from './core.js';

export function speechSupported() {
  return Boolean(window.SpeechRecognition || window.webkitSpeechRecognition);
}

export function listenForCommand(formType, callbacks = {}) {
  const Recognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!Recognition) {
    callbacks.onError?.('El reconocimiento de voz no está disponible en este navegador. Usa Google Chrome o Microsoft Edge.');
    return { stop() {} };
  }

  const recognition = new Recognition();
  recognition.lang = 'es-EC';
  recognition.interimResults = true;
  recognition.continuous = true;
  recognition.maxAlternatives = 3;

  let finalTranscript = '';
  let visibleTranscript = '';
  let finished = false;
  let maximumTimer = null;
  recognition.onstart = () => {
    maximumTimer = setTimeout(() => recognition.stop(), 30000);
    callbacks.onStart?.();
  };
  recognition.onresult = event => {
    let interim = '';
    for (let index = event.resultIndex; index < event.results.length; index += 1) {
      const transcript = event.results[index][0].transcript;
      if (event.results[index].isFinal) finalTranscript += ` ${transcript}`;
      else interim += ` ${transcript}`;
    }
    visibleTranscript = `${finalTranscript} ${interim}`.trim();
    callbacks.onTranscript?.(visibleTranscript, Boolean(finalTranscript));
  };
  recognition.onerror = event => {
    const messages = {
      'not-allowed': 'El navegador no tiene permiso para usar el micrófono.',
      'service-not-allowed': 'El servicio de voz está bloqueado en este navegador.',
      'no-speech': 'No se detectó voz. Intenta nuevamente y habla con claridad.',
      'audio-capture': 'No se encontró un micrófono disponible.',
      network: 'No se pudo acceder al servicio de reconocimiento de voz.'
    };
    callbacks.onError?.(messages[event.error] || `No se pudo reconocer la voz (${event.error}).`);
  };
  recognition.onend = () => {
    if (finished) return;
    finished = true;
    clearTimeout(maximumTimer);
    const transcript = (finalTranscript.trim() || visibleTranscript.trim());
    if (transcript) callbacks.onResult?.(parseVoiceCommand(transcript, formType));
    callbacks.onEnd?.();
  };

  try {
    recognition.start();
  } catch (error) {
    finished = true;
    clearTimeout(maximumTimer);
    callbacks.onError?.(error.message || 'No se pudo iniciar el micrófono.');
    callbacks.onEnd?.();
  }
  return {
    stop: () => {
      try { recognition.stop(); } catch { callbacks.onEnd?.(); }
    }
  };
}

export function voiceHelp(formType) {
  if (formType === 'maintenance') {
    return 'Dicta varios campos seguidos y luego pulsa “Finalizar dictado”. Ejemplo: “Activo EVOLQ 87, servicio preventivo, kilometraje 84520, horómetro 6341, acción cambio de aceite, orden OT 184”.';
  }
  if (formType === 'tread') {
    return 'Dicta varios campos y luego pulsa “Finalizar dictado”. Ejemplo: “Activo EVOLQ 87, proyecto Loja, posición 1 huella 9.2, posición 2 huella 9.1, estado bueno”.';
  }
  return 'Dicta varios campos y luego pulsa “Finalizar dictado”. Ejemplo: “Activo EVOLQ 87, proyecto Loja, cambio, posición 7, huella 5.8, marca Bridgestone, medida 12R22.5”.';
}
