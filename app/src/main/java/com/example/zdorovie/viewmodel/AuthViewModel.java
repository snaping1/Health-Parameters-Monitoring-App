package com.example.zdorovie.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.zdorovie.model.User;
import com.example.zdorovie.repository.UserRepository;
import com.example.zdorovie.repository.BaseRepository.Result;

import java.util.concurrent.CompletableFuture;

public class AuthViewModel extends ViewModel {
    private final UserRepository userRepository;
    
    private final MutableLiveData<AuthState> _authState = new MutableLiveData<>();
    private final MutableLiveData<User> _currentUser = new MutableLiveData<>();
    
    public AuthViewModel() {
        this.userRepository = new UserRepository();
        checkCurrentUser();
    }
    
    private void checkCurrentUser() {
        _authState.setValue(AuthState.Loading.INSTANCE);
        
        userRepository.getCurrentUser()
            .thenAccept(result -> {
                if (result.isSuccess()) {
                    User user = result.getData();
                    _currentUser.postValue(user);
                    _authState.postValue(user != null ? AuthState.Authenticated.INSTANCE : AuthState.Unauthenticated.INSTANCE);
                } else {
                    _authState.postValue(AuthState.Unauthenticated.INSTANCE);
                }
            });
    }
    
    public void signIn(String email, String password) {
        _authState.setValue(AuthState.Loading.INSTANCE);
        
        userRepository.signIn(email, password)
            .thenAccept(result -> {
                if (result.isSuccess()) {
                    User user = result.getData();
                    _currentUser.postValue(user);
                    _authState.postValue(AuthState.Authenticated.INSTANCE);
                } else {
                    String errorMessage = result.getException() != null ? 
                        result.getException().getMessage() : "Ошибка входа";
                    _authState.postValue(new AuthState.Error(errorMessage));
                }
            });
    }
    
    public void signUp(String email, String password, String name, User.UserRole role) {
        _authState.setValue(AuthState.Loading.INSTANCE);
        
        userRepository.signUp(email, password, name, role)
            .thenAccept(result -> {
                if (result.isSuccess()) {
                    User user = result.getData();
                    _currentUser.postValue(user);
                    _authState.postValue(AuthState.Authenticated.INSTANCE);
                } else {
                    String errorMessage = result.getException() != null ? 
                        result.getException().getMessage() : "Ошибка регистрации";
                    _authState.postValue(new AuthState.Error(errorMessage));
                }
            });
    }
    
    public void signOut() {
        userRepository.signOut();
        _currentUser.setValue(null);
        _authState.setValue(AuthState.Unauthenticated.INSTANCE);
    }
    
    public void updateCurrentUser(User user) {
        _currentUser.setValue(user);
        _authState.setValue(AuthState.Authenticated.INSTANCE);
    }
    
    public LiveData<AuthState> getAuthState() {
        return _authState;
    }
    
    public LiveData<User> getCurrentUser() {
        return _currentUser;
    }
    
    // Классы состояния аутентификации
    public static abstract class AuthState {
        private AuthState() {
            // Приватный конструктор, чтобы предотвратить создание экземпляров вне класса
        }
        
        public static final class Loading extends AuthState {
            private static final Loading INSTANCE = new Loading();
            
            private Loading() {
                super();
            }
        }
        
        public static final class Authenticated extends AuthState {
            private static final Authenticated INSTANCE = new Authenticated();
            
            private Authenticated() {
                super();
            }
        }
        
        public static final class Unauthenticated extends AuthState {
            private static final Unauthenticated INSTANCE = new Unauthenticated();
            
            private Unauthenticated() {
                super();
            }
        }
        
        public static final class Error extends AuthState {
            private final String message;
            
            public Error(String message) {
                super();
                this.message = message;
            }
            
            public String getMessage() {
                return message;
            }
            
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Error error = (Error) o;
                return message.equals(error.message);
            }
            
            @Override
            public int hashCode() {
                return message.hashCode();
            }
        }
    }
} 