import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface User {
  id: number;
  username: string;
  email: string;
  fullName?: string;
  avatarUrl?: string;
  role: 'ADMIN' | 'USER' | 'GUEST';
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'DELETED';
}

interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  token: string | null;
  login: (username: string, password: string) => Promise<void>;
  register: (data: RegisterData) => Promise<void>;
  logout: () => void;
  checkAuth: () => void;
  clearAuth: () => void;
  updateUser: (user: User) => void;
}

interface RegisterData {
  username: string;
  email: string;
  password: string;
  fullName?: string;
}

const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      isAuthenticated: false,
      user: null,
      token: null,

      login: async (username: string, password: string) => {
        const response = await import('@/api/auth').then((m) =>
          m.default.login({ username, password })
        );

        if (response.success && response.data) {
          set({
            isAuthenticated: true,
            user: response.data.user,
            token: response.data.accessToken,
          });
        }
      },

      register: async (data: RegisterData) => {
        const response = await import('@/api/auth').then((m) =>
          m.default.register(data)
        );

        if (response.success && response.data) {
          set({
            isAuthenticated: true,
            user: response.data.user,
            token: response.data.accessToken,
          });
        }
      },

      logout: () => {
        set({
          isAuthenticated: false,
          user: null,
          token: null,
        });
      },

      checkAuth: async () => {
        const token = useAuthStore.getState().token;
        if (!token) {
          set({ isAuthenticated: false, user: null });
          return;
        }

        try {
          const response = await import('@/api/auth').then((m) => m.default.getCurrentUser());
          if (response.success && response.data) {
            set({
              isAuthenticated: true,
              user: response.data,
            });
          } else {
            set({ isAuthenticated: false, user: null, token: null });
          }
        } catch (error) {
          set({ isAuthenticated: false, user: null, token: null });
        }
      },

      clearAuth: () => {
        set({ isAuthenticated: false, user: null, token: null });
      },

      updateUser: (user: User) => {
        set({ user });
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        isAuthenticated: state.isAuthenticated,
        user: state.user,
        token: state.token,
      }),
    }
  )
);

export default useAuthStore;
