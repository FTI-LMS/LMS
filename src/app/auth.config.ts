
import { Configuration, PublicClientApplication } from '@azure/msal-browser';

export const msalConfig: Configuration = {
  auth: {
    clientId: '60bcec07-ec66-42ed-862a-ce63428fd386', // Replace with your Azure AD app registration client ID
    authority: 'https://login.microsoftonline.com/3d2555d9-f56b-466e-bebc-f354c0bdd9b4', // Replace with your tenant ID
    redirectUri: 'http://localhost:4200' // Adjust for your deployment URL
  },
  cache: {
    cacheLocation: 'localStorage',
    storeAuthStateInCookie: false
  }
};

export const loginRequest = {
  scopes: ['openid', 'profile', 'User.Read']
};

export const msalInstance = new PublicClientApplication(msalConfig);

// Initialize MSAL instance
msalInstance.initialize().then(() => {
  // Handle the result of initialize
  const activeAccount = msalInstance.getActiveAccount();
  if (!activeAccount && msalInstance.getAllAccounts().length > 0) {
    msalInstance.setActiveAccount(msalInstance.getAllAccounts()[0]);
  }
});
