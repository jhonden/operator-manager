import request from '@/utils/request';
import type { ApiResponse, MarketItem, MarketSearchRequest, PageResponse, Review, RatingRequest, ReviewRequest } from '@/types';

export const marketApi = {
  /**
   * Search market items
   */
  searchItems: (data: MarketSearchRequest): Promise<ApiResponse<PageResponse<MarketItem>>> => {
    return request.post<ApiResponse<PageResponse<MarketItem>>>('/v1/market/search', data);
  },

  /**
   * Get market item by ID
   */
  getItem: (id: number): Promise<ApiResponse<MarketItem>> => {
    return request.get<ApiResponse<MarketItem>>(`/v1/market/items/${id}`);
  },

  /**
   * Get featured items
   */
  getFeaturedItems: (): Promise<ApiResponse<MarketItem[]>> => {
    return request.get<ApiResponse<MarketItem[]>>('/v1/market/featured');
  },

  /**
   * Get top rated items
   */
  getTopRatedItems: (): Promise<ApiResponse<MarketItem[]>> => {
    return request.get<ApiResponse<MarketItem[]>>('/v1/market/top-rated');
  },

  /**
   * Get most downloaded items
   */
  getMostDownloadedItems: (): Promise<ApiResponse<MarketItem[]>> => {
    return request.get<ApiResponse<MarketItem[]>>('/v1/market/most-downloaded');
  },

  /**
   * Get latest items
   */
  getLatestItems: (): Promise<ApiResponse<MarketItem[]>> => {
    return request.get<ApiResponse<MarketItem[]>>('/v1/market/latest');
  },

  /**
   * Publish operator to market
   */
  publishOperator: (operatorId: number): Promise<ApiResponse<MarketItem>> => {
    return request.post<ApiResponse<MarketItem>>(
      `/v1/market/publish/operator/${operatorId}`
    );
  },

  /**
   * Publish package to market
   */
  publishPackage: (packageId: number): Promise<ApiResponse<MarketItem>> => {
    return request.post<ApiResponse<MarketItem>>(
      `/v1/market/publish/package/${packageId}`
    );
  },

  /**
   * Unpublish from market
   */
  unpublish: (marketItemId: number): Promise<ApiResponse<void>> => {
    return request.delete<ApiResponse<void>>(`/v1/market/items/${marketItemId}`);
  },

  /**
   * Submit rating
   */
  submitRating: (marketItemId: number, data: RatingRequest): Promise<ApiResponse<void>> => {
    return request.post<ApiResponse<void>>(`/v1/market/items/${marketItemId}/rating`, data);
  },

  /**
   * Submit review
   */
  submitReview: (
    marketItemId: number,
    data: ReviewRequest
  ): Promise<ApiResponse<Review>> => {
    return request.post<ApiResponse<Review>>(
      `/v1/market/items/${marketItemId}/reviews`,
      data
    );
  },

  /**
   * Get reviews
   */
  getReviews: (marketItemId: number, page = 0, size = 20): Promise<ApiResponse<Review[]>> => {
    return request.get<ApiResponse<Review[]>>(`/v1/market/items/${marketItemId}/reviews`, {
      params: { page, size },
    });
  },

  /**
   * Like review
   */
  likeReview: (reviewId: number): Promise<ApiResponse<void>> => {
    return request.post<ApiResponse<void>>(`/v1/market/reviews/${reviewId}/like`);
  },

  /**
   * Download item (clone to workspace)
   */
  downloadItem: (marketItemId: number): Promise<ApiResponse<void>> => {
    return request.post<ApiResponse<void>>(`/v1/market/items/${marketItemId}/download`);
  },
};
