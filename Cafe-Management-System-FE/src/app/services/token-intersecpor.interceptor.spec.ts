import { TestBed } from '@angular/core/testing';

import { TokenIntersecporInterceptor } from './token-intersecpor.interceptor';

describe('TokenIntersecporInterceptor', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      TokenIntersecporInterceptor
      ]
  }));

  it('should be created', () => {
    const interceptor: TokenIntersecporInterceptor = TestBed.inject(TokenIntersecporInterceptor);
    expect(interceptor).toBeTruthy();
  });
});
