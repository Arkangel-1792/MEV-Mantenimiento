export default {
  async fetch(request) {
    const url = new URL(request.url);

    if (url.pathname === '/health') {
      return Response.json({
        application: 'MEV Mantenimiento Web',
        status: 'ok',
        version: '2.2.0',
      });
    }

    return new Response('Recurso no encontrado.', { status: 404 });
  },
};
