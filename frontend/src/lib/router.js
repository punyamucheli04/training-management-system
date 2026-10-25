import { useEffect, useState } from 'react';

// Very small hash router: #/students, #/attendance?session=401
// (keeps the project free of extra dependencies; refresh keeps the page).

function parse() {
  const hash = window.location.hash.replace(/^#/, '') || '/';
  const [path, queryString = ''] = hash.split('?');
  return { path: path || '/', params: Object.fromEntries(new URLSearchParams(queryString)) };
}

export function useRoute() {
  const [route, setRoute] = useState(parse);
  useEffect(() => {
    const onChange = () => setRoute(parse());
    window.addEventListener('hashchange', onChange);
    return () => window.removeEventListener('hashchange', onChange);
  }, []);
  return route;
}

export function navigate(path) {
  window.location.hash = path;
}
