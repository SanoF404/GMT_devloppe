/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useEffect, useState } from 'react';
import { adminApiService, adminAuthApi } from '../services/admin/api';

const AuthContext = createContext();

// Le jeton de session est un cookie httpOnly posé par le serveur : cette page ne le lit jamais.
// Seul le profil non sensible est gardé localement, pour afficher l'interface avant la vérification serveur.
const USER_KEY = 'user';
const LEGACY_TOKEN_KEYS = ['token', 'adminToken'];

const safeStorage = (action) => {
  try { action(); } catch { /* stockage indisponible (navigation privée, données bloquées) */ }
};

const clearLegacyTokens = () => {
  safeStorage(() => LEGACY_TOKEN_KEYS.forEach((key) => localStorage.removeItem(key)));
};

const readSavedUser = () => {
  try {
    const savedUser = localStorage.getItem(USER_KEY);
    return savedUser ? JSON.parse(savedUser) : null;
  } catch {
    safeStorage(() => localStorage.removeItem(USER_KEY));
    return null;
  }
};

const writeSavedUser = (userData) => {
  safeStorage(() => localStorage.setItem(USER_KEY, JSON.stringify(userData)));
};

const clearSavedUser = () => {
  safeStorage(() => localStorage.removeItem(USER_KEY));
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    clearLegacyTokens();
    return readSavedUser();
  });
  const [loading, setLoading] = useState(() => Boolean(readSavedUser()));

  // Au chargement, demande au serveur si le cookie de session est toujours valide.
  useEffect(() => {
    let active = true;
    const cached = readSavedUser();
    if (!cached) return undefined;
    adminApiService.profile({ preservePageOnUnauthorized: true })
      .then(({ data }) => {
        if (!active) return;
        const nextUser = { ...cached, id: data.id, nom: data.nom, prenom: data.prenom, email: data.email, role: data.role };
        writeSavedUser(nextUser);
        setUser(nextUser);
      })
      .catch((error) => {
        if (!active) return;
        const status = error.response?.status;
        if (status === 401 || status === 403) {
          clearSavedUser();
          setUser(null);
        }
      })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  const login = async (email, motDePasse) => {
    const response = await adminAuthApi.login({ email, motDePasse });
    clearLegacyTokens();
    writeSavedUser(response.data);
    setUser(response.data);
    return response.data;
  };

  // Le cookie est effacé côté serveur par l'appelant (adminApiService.logout) ; ici on nettoie l'état local.
  const logout = () => {
    clearLegacyTokens();
    clearSavedUser();
    setUser(null);
  };

  const updateAuthenticatedUser = (userData) => {
    writeSavedUser(userData);
    setUser(userData);
  };

  const isAdmin = () => {
    if (!user) return false;
    const role = typeof user.role === 'string' ? user.role : user.role?.name || user.role?.authority || '';
    return role.toUpperCase() === 'ROLE_ADMIN' || role.toUpperCase() === 'ADMIN';
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, updateAuthenticatedUser, isAdmin, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
